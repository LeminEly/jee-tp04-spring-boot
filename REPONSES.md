# Réponses — TP 04 : API REST et gestion des exceptions

Réponses aux questions de compréhension (section 6 de l'énoncé), précédées de
la question de l'exercice 5.2 et suivies des écarts constatés pendant le TP.
Les sorties citées sont dans [`docs/captures-curl.md`](docs/captures-curl.md).

---

## Exercice 5.2 — Pourquoi la première version de `getStudents()` est-elle une mauvaise pratique ?

La liste est construite **dans** la méthode : chaque appel à `GET /api/students`
alloue un nouvel `ArrayList` et trois nouveaux objets `Student`, qui deviennent
des déchets pour le ramasse-miettes dès la réponse envoyée. Le travail est
identique à chaque requête, donc inutile à partir de la deuxième.

Il y a aussi un problème de conception : la donnée n'a pas d'état stable. Rien
de ce qui serait fait sur la liste ne survivrait à l'appel suivant.

L'exercice 5.3 le corrige : la liste devient un champ, rempli une seule fois.
Un test le prouve : le bean renvoie **la même instance** de liste d'un appel à
l'autre. Ce test échouait sur la version 5.2.

---

## 1. Différence entre `@Controller` et `@RestController` ; rôle de `@ResponseBody`

Avec `@Controller`, la valeur renvoyée par une méthode est par défaut un **nom
de vue** (un gabarit HTML à rendre).

`@RestController` est la composition de `@Controller` et `@ResponseBody`.
`@ResponseBody` demande d'écrire la valeur renvoyée **directement dans le corps
de la réponse HTTP**, sans passer par une vue. Spring choisit un convertisseur
(`HttpMessageConverter`) selon le type de la valeur et l'en-tête `Accept` :
- une `String` part telle quelle, en `text/plain` : `GET /api/hello` renvoie
  `Content-Type: text/plain;charset=UTF-8` ;
- un objet ou une liste est sérialisé en JSON par Jackson : `GET /api/students`
  renvoie `Content-Type: application/json`.

## 2. Verbe HTTP idempotent

Une opération est idempotente si l'appeler **plusieurs fois produit le même
effet sur le serveur** qu'un seul appel.

- **Idempotent : `PUT`.** `PUT /api/students/2` avec le même corps remplace
  l'étudiant 2 par la même valeur : après un ou dix appels, l'état final est le
  même. `GET` (aucune modification) et `DELETE` (après la première suppression,
  la ressource reste absente) le sont aussi.
- **Non idempotent : `POST`.** `POST /api/students` crée une nouvelle ressource
  à chaque appel : dix appels créent dix étudiants.

Conséquence pratique : un client peut rejouer sans risque une requête
idempotente après une coupure réseau, pas un `POST`.

L'idempotence porte sur l'**état du serveur**, pas sur la réponse : un second
`DELETE` peut répondre 404 au lieu de 204 et rester idempotent.

## 3. Pourquoi `@PostConstruct` plutôt que le corps de `getStudents()` ?

`@PostConstruct` est exécutée **une seule fois**, juste après que le conteneur
Spring a construit le bean et injecté ses dépendances. Le contrôleur est un
singleton : la liste construite à ce moment sert à toutes les requêtes.

Le coût de la mauvaise version, détaillé plus haut (exercice 5.2) : une
allocation et trois objets par requête, du travail répété sans raison, et
aucune donnée stable. Ce coût n'a **pas été mesuré** dans ce TP : avec trois
objets, il est négligeable en absolu. Le défaut est de conception : il grandit
avec la taille des données et devient grave si l'initialisation coûte cher
(lecture d'un fichier, appel réseau).

`@PostConstruct` plutôt que le constructeur : la méthode s'exécute après
l'injection des dépendances, qui sont donc disponibles. L'import est
`jakarta.annotation.PostConstruct` : l'espace de noms `javax.*` n'existe plus
depuis Spring Boot 3.

## 4. Rôle de Jackson ; ce que change Spring Boot 4

Jackson fait la **(dé)sérialisation automatique** entre objets Java et JSON.
Dans ce TP, il transforme une `List<Student>` en tableau JSON et un
`StudentErrorResponse` en objet JSON, en lisant les **getters** : c'est pour ça
que les POJO doivent en avoir. Dans l'autre sens, il a besoin d'un
**constructeur sans argument** et de setters.

Spring Boot 4 passe de **Jackson 2 à Jackson 3** :
- les packages deviennent `tools.jackson.*` au lieu de `com.fasterxml.jackson.*`
  (par exemple `tools.jackson.databind.ObjectMapper` ; dans ce projet,
  `jackson-databind` 3.0.2). Les *mappers* sont désormais immuables et se
  configurent par un *builder* (`JsonMapper.builder()`) ;
- les annotations (`@JsonProperty`, `@JsonIgnore`…) restent dans
  `com.fasterxml.jackson.annotation` (artefact `jackson-annotations` 2.20 dans
  ce projet), pour la compatibilité ;
- le starter `spring-boot-starter-webmvc` (l'ancien `spring-boot-starter-web`)
  fournit Jackson 3 : ce TP n'a ajouté aucune dépendance.

Dans ce TP, aucune classe Jackson n'est importée : tout passe par la
configuration automatique.

## 5. `ResponseEntity<T>` par rapport à un simple retour d'objet

Un objet renvoyé seul part avec le code **200** et les en-têtes par défaut.
`ResponseEntity<T>` représente **toute la réponse HTTP** et permet de
contrôler :
1. **le code de statut** : `HttpStatus.NOT_FOUND` (404), `BAD_REQUEST` (400) ;
2. **les en-têtes** : `Location`, `Cache-Control`, `Content-Type`… ;
3. **le corps** : ici un `StudentErrorResponse`.

C'est indispensable pour une erreur : sans `ResponseEntity`, le handler
renverrait le corps JSON de l'erreur avec un code 200, ce qui est faux.

## 6. Comment Spring choisit l'`@ExceptionHandler` à invoquer

Quand une exception sort d'une méthode de contrôleur, Spring cherche un handler
dont le type déclaré (le type du paramètre, ou la valeur de l'annotation)
correspond à l'exception levée :
1. **Le plus spécifique gagne.** Il compare la distance dans la hiérarchie des
   classes. `StudentNotFoundException` est attrapée par le handler qui déclare
   ce type, et pas par le catch-all `Exception`, pourtant compatible lui aussi.
2. **Le contrôleur d'abord, puis les `@ControllerAdvice`.** Les handlers
   déclarés dans le contrôleur qui a levé l'exception sont essayés avant les
   handlers globaux. Entre plusieurs `@ControllerAdvice`, l'ordre suit `@Order`.
3. Si rien ne correspond, l'exception remonte et produit un 500.

Vérifié dans ce TP : `/api/students/999` donne 404 (handler spécifique) et
`/api/students/abc` donne 400. Dans ce dernier cas, la conversion de `"abc"` en
`int` lève `MethodArgumentTypeMismatchException`, que seul le catch-all accepte.

## 7. Le problème que résout `@ControllerAdvice` ; quand restreindre sa portée

Un `@ExceptionHandler` local ne s'applique qu'à **son** contrôleur. Avec dix
contrôleurs, il faudrait recopier dix fois les mêmes handlers. `@ControllerAdvice`
**centralise** la gestion des erreurs, une préoccupation transversale, dans une
seule classe appliquée à tous les contrôleurs. Les réponses d'erreur ont ainsi
un format unique, et les contrôleurs ne portent plus que leurs routes.

On restreint sa portée quand des contrôleurs n'ont pas les mêmes besoins.
Exemple : une application qui sert à la fois une API JSON et des pages HTML ne
doit pas renvoyer du JSON d'erreur à un navigateur. On cible alors :
- un package : `@ControllerAdvice(basePackages = "esp.irt.studenttracker.rest")` ;
- des classes : `assignableTypes = StudentRestController.class` ;
- une annotation : `annotations = RestController.class`.

Ce TP montre aussi le revers de la portée globale. Le catch-all `Exception`
attrape alors **tout**, y compris `NoResourceFoundException`, que Spring lève
pour une URL inconnue : `/hello` répondait **400** au lieu de 404. Voir les
écarts plus bas.

## 8. Pourquoi un 404 avec un corps JSON structuré plutôt qu'un 500

Pour le **consommateur de l'API** (front web, application mobile, script) :
- **Le code dit qui est en faute.** Un 404 dit « cette ressource n'existe pas » :
  c'est une erreur du client, normale et attendue. Un 500 dit « le serveur est
  en panne ». Le client ne peut pas distinguer un étudiant inexistant d'une
  vraie panne, et risque de réessayer en boucle une requête qui n'aboutira
  jamais.
- **Le corps se lit par programme.** `{status, message, timeStamp}` a toujours
  la même forme : le client peut afficher le message et journaliser l'heure
  sans analyser du texte libre.
- **Le serveur n'expose pas ses détails internes.** Une trace de pile révèle les
  classes, les bibliothèques et parfois leurs versions : c'est utile à un
  attaquant, et inutile au client.
- **La supervision reste lisible.** Les 500 signalent de vrais incidents. Les
  noyer sous des 404 déguisés rend les alertes inutiles.

---

## Écarts et limites constatés pendant le TP

### Le 500 de l'exercice 5.4 ne contient pas de trace

L'énoncé annonce « code 500 avec une trace ». Avec Spring Boot 4, le client
reçoit un 500 avec un JSON générique
`{"timestamp":…,"status":500,"error":"Internal Server Error","path":"/api/students/999"}`,
**sans** trace de pile : `server.error.include-stacktrace` vaut `never` par
défaut. La trace (`IndexOutOfBoundsException: Index 999 out of bounds for length 3`)
n'apparaît que dans le journal du serveur. La réponse reste inexploitable pour
le client, pour les raisons de la question 8.

### Écart au code de l'énoncé : un handler 404 pour les URL inconnues (exercice 5.7)

En passant au `@ControllerAdvice` global, le catch-all `Exception → 400` s'est
mis à attraper `NoResourceFoundException`. Constaté : `/hello`, `/api/inconnu`
→ **400** « No static resource … », alors que c'était 404 avec les handlers
locaux de la 5.6.

Un troisième handler, `NoResourceFoundException → 404`, rétablit le bon code.
Le reste du code de l'énoncé est intact. Un test l'exige ; il échoue (400 au
lieu de 404) si on retire ce handler.

### Limites du catch-all de l'énoncé

Le handler `Exception → 400` est écrit comme l'énoncé le demande. C'est un
choix pédagogique, pas une pratique de production :
- **Il expose des détails internes.** Il renvoie `exc.getMessage()` au client.
  Pour `/api/students/abc` : *« Method parameter 'studentId': Failed to convert
  value of type 'java.lang.String' to required type 'int'… »*, soit le nom du
  paramètre Java et les types internes.
- **Il ment sur l'origine des pannes.** Une vraie erreur serveur (base
  indisponible, `NullPointerException`) serait renvoyée en **400**, une faute
  du client, au lieu de 500, et ne déclencherait aucune alerte.

En production, on attraperait les exceptions de requête invalide par leur type
(`MethodArgumentTypeMismatchException`…) pour un 400 avec un message neutre, et
on laisserait un dernier handler renvoyer un 500 générique en journalisant la
cause.

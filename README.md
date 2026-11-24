# Student Tracker — TP 04 : API REST et gestion des exceptions

Service web Spring Boot 4 qui publie en JSON la liste des étudiants inscrits et
permet d'en consulter un par son index, avec des erreurs 404 et 400 structurées.

Module JEE (IRT), chapitre 4, partie 1.

## Démarrer

Prérequis : **JDK 25**. Maven n'est pas nécessaire : le wrapper `./mvnw` le
télécharge.

```sh
./mvnw spring-boot:run          # http://localhost:8080
./mvnw verify                   # formatage, compilation, tests
./mvnw spotless:apply           # reformate le code Java
```

## Routes

| Requête | Réponse |
| --- | --- |
| `GET /api/hello` | `200` — `Hello World` (texte) |
| `GET /api/students` | `200` — tableau JSON des trois étudiants |
| `GET /api/students/{studentId}` | `200` — l'étudiant à cet index (0 à 2) |
| index hors bornes (`/999`, `/3`, `/-1`) | `404` — `{status, message, timeStamp}` |
| identifiant non numérique (`/abc`) | `400` — `{status, message, timeStamp}` |
| URL inconnue | `404` — `{status, message, timeStamp}` |

`studentId` est l'**index** dans une liste en mémoire, pas un identifiant
stable. Les données sont rechargées à chaque démarrage.

## Structure

```
src/main/java/esp/irt/studenttracker/
├── StudentTrackerApplication.java
├── model/Student.java                      POJO
└── rest/
    ├── StudentRestController.java          routes /api
    ├── StudentNotFoundException.java       exception métier
    ├── StudentErrorResponse.java           corps des erreurs
    └── StudentRestExceptionHandler.java    @ControllerAdvice global
```

## Livrables

- Code source : ce dépôt.
- Sorties `curl` : [`docs/captures-curl.md`](docs/captures-curl.md).
- Réponses aux questions : [`REPONSES.md`](REPONSES.md).

## Ce que le service ne fait pas

Aucune persistance, lecture seule (`GET` uniquement), aucune authentification,
aucun déploiement. Ces limites sont voulues par l'énoncé.

## Contribuer

Voir [`CONTRIBUTING.md`](CONTRIBUTING.md) : branches, format des commits et
contrôles locaux (`pre-commit install`).

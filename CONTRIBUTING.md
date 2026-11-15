# Contribuer

## Avant de commencer

- JDK 25 et `./mvnw` (aucun Maven à installer).
- `pre-commit install` — pose les crochets `pre-commit` et `commit-msg`.
- Lire l'énoncé du TP : il fixe les noms de classes, de packages et de routes.

## Cycle

1. Une branche par exercice ou par correction : `feat/`, `fix/`, `docs/`,
   `chore/`, `test/`. Jamais de commit direct sur `main`.
2. Un commit atomique : il fait une seule chose, clairement.
3. Message au format Conventional Commits ; il explique **le problème et la
   décision**, pas la liste des fichiers.
4. `./mvnw verify` passe avant d'ouvrir une demande de fusion. Les crochets
   locaux ne se contournent jamais.

## Commandes

```sh
./mvnw spring-boot:run     # démarre sur http://localhost:8080
./mvnw verify              # format, compilation, tests
./mvnw spotless:apply      # reformate le code Java
```

## Ce qui est refusé

- Un commit qui mentionne un outil ou un agent d'assistance, un nom de personne,
  ou qui contient un emoji.
- Un renommage des classes, packages ou routes imposés par l'énoncé.
- Une dépendance ajoutée sans justification écrite.
- Un correctif qui traite le symptôme sans que la cause ait été prouvée.
- Une réponse d'erreur qui expose une trace de pile.

## Tests

Toute route a son test MockMvc qui vérifie le code HTTP et le corps. Toute
correction de bug arrive avec le test qui échoue avant elle.

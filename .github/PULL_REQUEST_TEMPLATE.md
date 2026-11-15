## Problème

Ce qui n'allait pas, ou ce qui manquait. Pas la liste des fichiers.

## Décision

Ce qui a été fait, et pourquoi cette approche plutôt qu'une autre.

## Cause prouvée

Pour une correction : comment la cause a été établie.

## Vérifications

```
[ ] ./mvnw verify vert (format, compilation, tests)
[ ] Chaque route touchée : test MockMvc sur le code HTTP et le corps
[ ] Sortie curl conforme à l'énoncé
[ ] Aucune trace de pile renvoyée au client
[ ] Aucun secret, aucun fichier de travail
[ ] Erreur découverte consignée au registre
```

## Ce que cette demande ne fait pas

La frontière, écrite.

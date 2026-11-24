# Captures curl

Sorties réelles, capturées le 2026-11-24 sur le commit `343860a` de `main`,
application démarrée par `./mvnw spring-boot:run` (« Tomcat started on port
8080 », « Started StudentTrackerApplication »). Seuls les espaces en fin de
ligne ont été retirés.

## Point d'entrée minimal (exercice 5.1)

```http
$ curl -i http://localhost:8080/api/hello
HTTP/1.1 200
Content-Type: text/plain;charset=UTF-8
Content-Length: 11
Date: Tue, 24 Nov 2026 11:07:48 GMT

Hello World
```

## Liste des étudiants (livrable)

```http
$ curl -i http://localhost:8080/api/students
HTTP/1.1 200
Content-Type: application/json
Transfer-Encoding: chunked
Date: Tue, 24 Nov 2026 11:07:48 GMT

[{"firstName":"Poornima","lastName":"Patel"},{"firstName":"Mario","lastName":"Rossi"},{"firstName":"Mary","lastName":"Smith"}]
```

## Étudiant valide (livrable)

```http
$ curl -i http://localhost:8080/api/students/2
HTTP/1.1 200
Content-Type: application/json
Transfer-Encoding: chunked
Date: Tue, 24 Nov 2026 11:07:48 GMT

{"firstName":"Mary","lastName":"Smith"}
```

## Index hors bornes → 404 (livrable)

```http
$ curl -i http://localhost:8080/api/students/999
HTTP/1.1 404
Content-Type: application/json
Transfer-Encoding: chunked
Date: Tue, 24 Nov 2026 11:07:48 GMT

{"status":404,"message":"Student id not found - 999","timeStamp":1795518468125}
```

## Identifiant non numérique → 400 (livrable)

```http
$ curl -i http://localhost:8080/api/students/abc
HTTP/1.1 400
Content-Type: application/json
Transfer-Encoding: chunked
Date: Tue, 24 Nov 2026 11:07:48 GMT
Connection: close

{"status":400,"message":"Method parameter 'studentId': Failed to convert value of type 'java.lang.String' to required type 'int'; For input string: \"abc\"","timeStamp":1795518468138}
```

## URL inconnue → 404 (écart à l'énoncé, voir REPONSES.md)

```http
$ curl -i http://localhost:8080/api/inconnu
HTTP/1.1 404
Content-Type: application/json
Transfer-Encoding: chunked
Date: Tue, 24 Nov 2026 11:07:48 GMT

{"status":404,"message":"No static resource api/inconnu for request '/api/inconnu'.","timeStamp":1795518468150}
```

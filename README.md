# toggle

A REST API for managing feature toggles, built with [http4k](https://www.http4k.org/) in Kotlin.

## API

All endpoints are under `/toggle`.

### Create a toggle

```
POST /toggle/{name}?enabled=true|false
```

Creates a named toggle with an initial enabled state. Returns `201 Created` with a `Location` header pointing to the new resource.

### Get a toggle

```
GET /toggle/{name}
```

Returns the current state of the toggle. Returns `404` if the toggle does not exist.

```json
{ "enabled": true }
```

### Get all toggles

```
GET /toggle
```

Returns all toggles and their current states as a JSON object.

```json
{ "my-feature": true, "another-feature": false }
```

### Enable a toggle

```
POST /toggle/{name}/enable
```

Enables an existing toggle. Returns `404` if the toggle does not exist.

### Disable a toggle

```
POST /toggle/{name}/disable
```

Disables an existing toggle. Returns `404` if the toggle does not exist.

### Delete a toggle

```
DELETE /toggle/{name}
```

Deletes an existing toggle. Returns `404` if the toggle does not exist.

## Requirements

- JDK 25

## Building

```bash
./gradlew build
```

## Running

```bash
./gradlew run
```

The server starts on port `10800` by default.

## Testing

```bash
./gradlew test        # unit tests
./gradlew e2eTest     # end-to-end tests (starts a real server)
./gradlew check       # both
```

To run a single test class:

```bash
./gradlew test --tests "io.hwalrus.toggle.AppTest"
```

## Tech stack

| Concern | Library |
|---|---|
| HTTP server | [http4k](https://www.http4k.org/) + Netty |
| JSON | Jackson (via http4k-format-jackson) |
| Unit/integration tests | [Kotest](https://kotest.io/) |
| E2E HTTP client | [OkHttp](https://square.github.io/okhttp/) |
| Build | Gradle 9.4.1 with version catalog |

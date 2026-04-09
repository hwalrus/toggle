# toggle

A REST API for managing feature toggles grouped by namespace, built with [http4k](https://www.http4k.org/) in Kotlin.

## API

Toggles are scoped to groups. Toggle names only need to be unique within a group.

### Groups

#### List groups

```
GET /group
```

Returns a sorted JSON array of group names.

```json
["payments", "ui"]
```

#### Create a group

```
POST /group/{group}
```

Returns `201 Created` with a `Location` header. Returns `409 Conflict` if the group already exists.

#### Rename a group

```
POST /group/{group}/rename?name={newName}
```

Renames the group and preserves all its toggles. Returns `404` if the group does not exist.

#### Delete a group

```
DELETE /group/{group}
```

Deletes the group and all its toggles. Returns `404` if the group does not exist.

---

### Toggles

All toggle endpoints are nested under a group.

#### Create a toggle

```
POST /group/{group}/toggle/{name}?enabled=true|false
```

Creates a named toggle with an initial enabled state. Returns `201 Created` with a `Location` header. Returns `404` if the group does not exist.

#### Get a toggle

```
GET /group/{group}/toggle/{name}
```

Returns the current state of the toggle. Returns `404` if the toggle or group does not exist.

```json
{ "enabled": true }
```

#### Get all toggles in a group

```
GET /group/{group}/toggle
```

Returns all toggles in the group and their current states as a JSON object. Returns `404` if the group does not exist.

```json
{ "my-feature": true, "another-feature": false }
```

#### Enable a toggle

```
POST /group/{group}/toggle/{name}/enable
```

Enables an existing toggle. Returns `404` if the toggle does not exist.

#### Disable a toggle

```
POST /group/{group}/toggle/{name}/disable
```

Disables an existing toggle. Returns `404` if the toggle does not exist.

#### Delete a toggle

```
DELETE /group/{group}/toggle/{name}
```

Deletes an existing toggle. Returns `404` if the toggle does not exist.

---

## Requirements

- JDK 25
- Node.js (for the web UI)
- Docker (for containerised builds and running)

## Building

```bash
./gradlew build
```

This also builds the frontend and packages it into the JAR under `public/`.

## Running

```bash
./gradlew run
```

The server starts on port `10800` by default and serves both the API and the web UI at [http://localhost:10800](http://localhost:10800).

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

Three layers of backend tests are in place:

| Test class | Type | What it covers |
|---|---|---|
| `InMemoryToggleStoreTest` | Unit | Store contract — groups and toggle operations |
| `AppTest` | Unit | HTTP routes — status codes, JSON responses, headers, CORS, security headers |
| `AppConfigTest` | Unit | HOCON config files — port defaults, allowed-origin resolution per environment |
| `ToggleEndToEndTest` | E2E | Full stack — real Netty server, real HTTP via OkHttp |

## Web UI

The frontend lives in `web/` and is built with React 19, TypeScript, and Vite.

### Install dependencies

```bash
cd web && npm install
```

### Development (hot reload)

Run the backend and frontend in separate terminals:

```bash
# Terminal 1 — backend
./gradlew run

# Terminal 2 — frontend
cd web && npm run dev
```

The UI is available at [http://localhost:5173](http://localhost:5173) and proxies API requests to the backend at `10800`.

### Build

```bash
cd web && npm run build
```

Output goes to `web/dist/`. The Gradle build runs this automatically.

To clean and rebuild from scratch:

```bash
cd web && rm -rf dist && npm run build
```

### Testing

```bash
cd web && npm test              # run all UI tests once
cd web && npm run test:watch    # watch mode (re-runs on file save)
cd web && npm run test:coverage # run with coverage report
```

Seven layers of frontend tests are in place (Vitest + React Testing Library):

| Test file | What it covers |
|---|---|
| `api.test.ts` | Fetch wrappers — correct URLs, methods, response mapping, error handling |
| `AddGroupForm.test.tsx` | Group form validation, submission, error display |
| `AddToggleForm.test.tsx` | Toggle form validation, submission, loading state, error display |
| `GroupSection.test.tsx` | Group header, inline rename, two-step delete confirmation |
| `ToggleList.test.tsx` | Loading state, empty state, and list rendering |
| `ToggleRow.test.tsx` | Enable/disable switch, two-step delete confirmation, error handling |
| `App.test.tsx` | Mount fetch, group rendering, error banner, empty state, add-then-refresh cycle |

### Type check

```bash
cd web && npm run build   # tsc -b runs as part of the build
```

## Docker

The app can be built and run as a container. The image uses a three-stage build (Node → JDK → JRE) and runs on **Eclipse Temurin 25 JRE + Alpine** as the minimal, security-hardened runtime base.

### Build and run with Docker Compose

```bash
docker compose up --build
```

The app is available at [http://localhost:10800](http://localhost:10800).

### Build and run standalone

```bash
docker build -t toggle .
docker run -p 10800:10800 toggle
```

### Environment variables

| Variable | Default | Description |
|---|---|---|
| `APP_ENV` | `local` | Selects the HOCON config file to load (`application-{APP_ENV}.conf`). Use `production` for deployed environments. |
| `ALLOWED_ORIGIN` | *(unset)* | Restricts CORS to a single origin (e.g. `https://my-app.example.com`). Read from `application-production.conf` via `${?ALLOWED_ORIGIN}`. When unset, all origins are permitted (suitable for local use only). |

### Inspect the image

```bash
docker images toggle      # check final image size
docker history toggle     # inspect layers
```

## Tech stack

| Concern | Library |
|---|---|
| HTTP server | [http4k](https://www.http4k.org/) + Netty |
| JSON | Jackson (via http4k-format-jackson) |
| Configuration | [Lightbend Config](https://github.com/lightbend/config) (HOCON) |
| Unit/integration tests | [Kotest](https://kotest.io/) |
| E2E HTTP client | [OkHttp](https://square.github.io/okhttp/) |
| Build | Gradle 9.4.1 with version catalog |
| Frontend | React 19 + TypeScript + [Vite](https://vite.dev/) |

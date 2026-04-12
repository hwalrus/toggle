# mongo

A standalone MongoDB instance for local development with the toggle app.

## Start

```bash
docker compose up -d
```

MongoDB will be available at `mongodb://localhost:27017`. Data is persisted in a named Docker volume (`mongo_mongo-data`) and survives container restarts.

## Stop

```bash
docker compose down
```

To also delete all stored data:

```bash
docker compose down -v
```

## Usage with the toggle app

Run the toggle app pointing at this instance:

```bash
APP_ENV=production MONGODB_URI=mongodb://localhost:27017 ./gradlew run
```

Or set the variable in your shell and run the tests:

```bash
APP_ENV=production MONGODB_URI=mongodb://localhost:27017 ./gradlew e2eTest
```

## Connect with mongosh

```bash
docker compose exec mongo mongosh
```

The toggle app stores data in the `toggle` database, `toggles` collection:

```js
use toggle
db.toggles.find()
```

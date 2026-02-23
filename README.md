# CryptoOrder
Spring Server which handles basic function that is necessary to Crypto Exchange

## Database Configuration

### Runtime (Server)
- `src/main/resources/application.properties` is git-ignored.
- Use `src/main/resources/application.properties.example` as the template.
- `src/main/resources/application.properties` imports `.env` automatically with:
  - `spring.config.import=optional:file:.env[.properties]`
- Server runtime uses PostgreSQL (including Azure Database for PostgreSQL).
- Recommended flow:
  - `cp .env.example .env`
  - edit `.env` values
  - run `./gradlew bootRun`
- You do not need `source .env` manually.
- Key env vars:
  - `DB_URL` (example: `jdbc:postgresql://<server>.postgres.database.azure.com:5432/<db>?sslmode=require`)
  - `DB_USERNAME`
  - `DB_PASSWORD`
  - optional: `DB_SCHEMA` (default `public`, example: `cryptoorder_app`)
  - optional: `JPA_DDL_AUTO` (default `update`)
- For local runs, `.env.example` enables ephemeral keys by default:
  - `AUTH_ALLOW_EPHEMERAL_KEYS=true`
  - `IDEMPOTENCY_ALLOW_EPHEMERAL_ENCRYPTION_KEY=true`
- In production, set both values to `false` and provide fixed key values.

### Tests
- Tests use in-memory H2 (`src/test/resources/application-test.properties`).
- Run tests:
  - `./gradlew test`

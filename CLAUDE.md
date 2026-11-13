# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Auxiliary project for the UTEM parallel computing workshop (Spanish-language codebase and comments). A small Spring Boot REST API serving daily USD exchange rates (Chile, Colombia, Ecuador) from PostgreSQL. Three parts:

- `app/` — Spring Boot 4.1 / Java 25 Maven project, packaged as a **WAR** (`finalName` = `cpyd`, context path `/cpyd`).
- `db/01-model.sql` — PostgreSQL schema (`credentials`, `exchanges`). Not auto-applied in production (`spring.jpa.hibernate.ddl-auto` is only set for tests); load it manually into the `exchangesdb` database.
- `tools/conversion.py` — generates the exchange-rate CSV with yfinance (`--start`, `--end`, `--output`). Ecuador is hardcoded to 1.00 (dollarized). `tools/venv/` is a checked-in-looking virtualenv; ignore it.

## Commands

Run from `app/` using the Maven wrapper:

```
./mvnw spring-boot:run          # run locally (needs Postgres at jdbc:postgresql:exchangesdb, user/pass utem/utem)
./mvnw package                  # build target/cpyd.war
./mvnw test                     # all tests (H2 in PostgreSQL mode, see src/test/resources/application.properties)
./mvnw test -Dtest=CpydApplicationTests            # single test class
./mvnw test -Dtest=CpydApplicationTests#method     # single test method
```

Swagger UI is provided by springdoc; actuator is enabled.

## Architecture

Package root `cl.utem.inf.cpyd`, layered: `api/v1` (controllers) → `manager` (services) → `domain/repository` (Spring Data JPA) → `domain/model` (entities extending `PkEntityBase`). `domain/data` holds request/response DTOs; `domain/utils` has entity helpers/audit clock.

**Auth flow (two steps, spans several files):**
1. `POST /v1/login/authenticate` (`LoginRest`) takes email + API key, checks them against `Credential`, and returns a JWT built by `JwtUtils.makeJwt` (nimbus-jose-jwt) carrying the credential's `token` UUID in claim `tokenId`.
2. Data endpoints (`ExchangeRest`, `GET /v1/exchanges/{country}` with `start_date`/`end_date`, and `/{country}/{exchange_date}`) require an `Authorization` header and call `AuthManager.validate(...)` manually in each handler — there is no Spring Security/filter. New endpoints must call it themselves.
3. The JWT signing secret is `cpyd.sign` (from `CPYD_SIGN`; empty by default so login fails until set — tests use a 32-char value).

**Caching:** `CacheConf` builds a dynamic Caffeine `CacheManager` (min 17 min expiry, min 100 entries per cache); caches are created on first use by name. `@Cacheable/@CacheEvict` live in `manager/helper/CacheHelper` (not in managers) deliberately, to avoid Spring proxy self-invocation. Credential/active-email lookups go through it (caches `active-emails`, `credential-by-token`, `credential-by-email`).

**Errors:** custom exceptions in `exceptions/` (`AuthException`→401, `ForbiddenException`→403, `ValidationException`→400, `UtemException`→422, `NoDataException`→404, `TooManyRequestsException`→429) are mapped to RFC 7807 `ProblemDetail` by `api/ApiExceptionHandler`, which also extends `ResponseEntityExceptionHandler` so framework errors (missing header, bad param types) return 4xx; anything else is a generic 500.

**Country:** `domain/enums/Country` maps to a `smallint` column via `CountryConverter`; `ExchangeRest.convert` maps the path string to the enum by hand.

## Notes

- Config comes from env vars: `CPYD_SIGN` (JWT secret, ≥32 bytes, required), `CPYD_DB_URL`, `CPYD_DB_USER`, `CPYD_DB_PASSWORD`, `CPYD_LOG_LEVEL`.
- `Country` is persisted via `CountryConverter` using the stable `Country.getId()` (0/1/2), not the enum ordinal; keep ids unchanged.
- Caches are created dynamically (any name works) with the shared Caffeine policy; empty credential lookups are not cached.
- `LoginAttemptManager` throttles failed logins per email (→ 429); `ApiExceptionHandler` extends `ResponseEntityExceptionHandler` so framework errors return 4xx.
- API keys are still stored in plain text in `credentials.api_key` (compared in constant time).
- `ApiFlowTests` is the end-to-end MockMvc test (H2); it seeds its own credentials/exchanges.
- The CSV loader was removed; load exchange data into the DB externally (e.g. from `tools/conversion.py` output).

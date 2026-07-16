# Event Platform Backend

Backend for Summer TechFest / event platform: Java 21, Spring Boot 3, Maven, PostgreSQL, Flyway, Lombok, MapStruct, Apache POI.

## Stack

- Java 21
- Spring Boot 3.3.x
- Maven
- PostgreSQL 16
- Spring Web / Data JPA / Security / Mail / AOP
- Flyway
- Lombok, MapStruct, Apache POI
- Testcontainers (integration tests)

## Features

- Participant registration (create-or-update by `email`)
- Multi-event links without duplicate `participant_events`
- Public events list and gallery
- Admin CRUD for events (Basic Auth)
- Admin Excel export of participants
- Optional email notification after registration
- Docker Compose: PostgreSQL + backend + frontend

## Requirements

- JDK 21
- Maven 3.9+
- Docker and Docker Compose (for container run / IT)
- Sibling frontend repo (only if building the `frontend` service): `EventConstructorFront` next to this project by default

Expected layout for full Compose stack:

```text
parent/
├── EventConstructor/          ← this repo (compose runs from here)
└── EventConstructorFront/     ← frontend (build context: ../EventConstructorFront)
```

If the frontend path differs, change `frontend.build.context` in `docker-compose.yml` (or introduce an env like `FRONTEND_CONTEXT` for DevOps).

## Configuration

Main settings: `src/main/resources/application.yml`.

Docker profile: `src/main/resources/application-docker.yml` (DB/mail/CORS from env).

### Local DB (default `application.yml`)

| Setting  | Value                 |
|----------|-----------------------|
| database | `event_platform`      |
| username | `event_app`           |
| password | `event_app_password`  |
| host     | `localhost:5432`      |

### Admin (HTTP Basic)

| Setting  | Value   |
|----------|---------|
| username | `admin` |
| password | `admin` |

Configured via `app.admin.*` (not the `admin_users` table).

### Docker DB

| Setting  | Value            |
|----------|------------------|
| database | `event_platform` |
| username | `postgres`       |
| password | `postgres`       |
| host     | service `postgres` |

## Build

```bash
mvn clean package
```

Skip tests if needed:

```bash
mvn clean package -DskipTests
```

The backend `Dockerfile` **does not** run Maven. It only copies `target/*.jar`. Always package before rebuilding the backend image after code changes.

## Run Locally (without Docker app)

1. Create PostgreSQL DB `event_platform` and user matching `application.yml`.
2. Start:

```bash
mvn spring-boot:run
```

API: `http://localhost:8080`

## Docker

Run all commands from this repository root:

```bash
cd EventConstructor
```

### 1. Build the jar

```bash
mvn clean package -DskipTests
```

### 2. Start PostgreSQL + backend

```bash
docker compose up -d --build postgres backend
```

### 3. Start frontend (optional)

Requires sibling `../EventConstructorFront`:

```bash
docker compose up -d --build frontend
```

### Or everything at once

```bash
mvn clean package -DskipTests
docker compose up -d --build
```

### Services and ports

| Service    | Port | URL / notes                                      |
|------------|------|--------------------------------------------------|
| postgres   | 5432 | healthcheck; backend waits until healthy         |
| backend    | 8080 | `http://localhost:8080`                          |
| frontend   | 3000 | `http://localhost:3000` (nginx proxies `/api/`)  |

Frontend image is built with empty `PUBLIC_API_BASE_URL` (same-origin). Browser calls `http://localhost:3000/api/...`; nginx inside the frontend container proxies to `http://backend:8080/api/`.

Gallery files are mounted: `./data/gallery` → `/app/data/gallery`.

### Useful checks

```bash
docker compose ps
curl http://localhost:8080/api/events
curl http://localhost:3000/api/events
```

### Stop

```bash
docker compose down
```

Data volume `postgres_data` is kept unless you run `docker compose down -v`.

## Tests

Unit + integration (Testcontainers PostgreSQL; Docker required for IT):

```bash
mvn test
```

Integration tests use profile `test` and a shared Postgres Testcontainer. No H2.

## API (short)

### Public

- `GET /api/events`
- `GET /api/gallery`, `GET /api/gallery/{filename}`
- `POST /api/participants/register`

### Admin (Basic Auth)

- `GET/POST /api/admin/events`, `GET/PUT/DELETE /api/admin/events/{id}`
- `GET /api/admin/export`
- `DELETE /api/admin/participants`

### Registration example

```http
POST /api/participants/register
Content-Type: application/json
```

```json
{
  "firstName": "Ivan",
  "lastName": "Petrov",
  "email": "test@test.com",
  "company": "Acme",
  "projectRole": "Backend Developer",
  "stack": "Java, Spring",
  "grade": "Middle",
  "telegram": "@ivan",
  "eventIds": ["11111111-1111-1111-1111-111111111111"],
  "personalDataConsent": true,
  "photoConsent": true,
  "newsletterConsent": false
}
```

Behavior:

- new email → create participant
- existing email → update profile fields
- add missing event links only (no duplicates, old links are not removed)

### Event create/update (admin) example

```json
{
  "title": "Meetup",
  "description": "Short text",
  "extendedDescription": "Long text for landing",
  "eventDate": "2026-08-15",
  "location": "Moscow",
  "registrationEnabled": true,
  "sortOrder": 1
}
```

## Security

Public (no auth): events, gallery, registration.

Protected: `/api/admin/**` — HTTP Basic (`admin` / `admin` by default).

## Notes

- Flyway migrations live in `src/main/resources/db/migration`
- `admin_users` table exists in schema; runtime admin auth uses `application.yml` / env
- For DevOps deploy without sibling folders, prefer publishing backend/frontend images to a registry and referencing `image:` instead of local `build.context`

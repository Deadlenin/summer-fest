# Event Platform Backend

Backend for an event platform based on Java 21, Spring Boot 3, Maven, PostgreSQL, Flyway, Lombok, MapStruct, and Apache POI.

## Stack

- Java 21
- Spring Boot 3.x
- Maven
- PostgreSQL
- Spring Web
- Spring Data JPA
- Spring Security
- Hibernate
- Flyway
- Lombok
- MapStruct
- Apache POI

## Features

- Participant registration with create-or-update behavior by `email`
- Linking participants to multiple events without duplicate registrations
- Flyway database migrations
- Admin Excel export of participant data
- Basic authentication for admin endpoints
- Docker support for backend and PostgreSQL

## Project Structure

```text
com.example.eventplatform
├── config
├── controller
├── dto
├── entity
├── repository
├── service
├── mapper
├── exception
└── security
```

## Requirements

- JDK 21
- Maven 3.9+
- PostgreSQL 15+ (or compatible)
- Docker and Docker Compose (optional)

## Configuration

Main settings are stored in `src/main/resources/application.yml`.

Default local database settings:

- database: `eventplatform`
- username: `postgres`
- password: `postgres`

Default admin credentials:

- username: `admin`
- password: `admin`

For Docker, the application uses `src/main/resources/application-docker.yml` and reads database connection settings from environment variables.

## Database Migrations

Flyway creates the following tables:

- `participants`
- `events`
- `participant_events`
- `admin_users`

## Build

```bash
mvn clean package
```

## Run Locally

1. Create a PostgreSQL database named `eventplatform`.
2. Check credentials in `src/main/resources/application.yml`.
3. Start the application:

```bash
mvn spring-boot:run
```

## Run With Docker Profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

## Docker

1. Build the application:

```bash
mvn clean package
```

2. Start containers:

```bash
docker compose up --build
```

Docker services:

- `backend` on port `8080`
- `postgres` on port `5432`

PostgreSQL in Docker:

- database: `event_platform`
- username: `postgres`
- password: `postgres`

The `postgres` container includes a healthcheck, and the backend starts only after the database becomes healthy.

## API

### Participant Registration

Endpoint:

```http
POST /api/participants/register
```

Request example:

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
  "eventIds": [
    "11111111-1111-1111-1111-111111111111"
  ]
}
```

Behavior:

- if participant with the given `email` does not exist, a new participant is created
- if participant already exists, their profile data is updated
- selected events are added to `participant_events`
- old participant-event links are never removed automatically
- duplicate participant-event links are not created

### Admin Export

Endpoint:

```http
GET /api/admin/export
```

Authentication:

- HTTP Basic Auth
- username and password are configured in `application.yml`

The endpoint returns an Excel file with:

- last name
- first name
- company
- role
- stack
- grade
- email
- telegram
- selected events
- registration date

## Security

- `/api/admin/**` requires HTTP Basic Authentication
- all non-admin endpoints are currently public

## Notes

- `admin_users` table exists in the schema, but current admin authentication is configured from `application.yml`
- Maven commands were not executed in this environment because `mvn` was unavailable in `PATH`

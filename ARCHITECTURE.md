# Техническая документация backend: Event Platform / Summer TechFest

**Проект:** `eventplatform` (`com.example:eventplatform:0.0.1-SNAPSHOT`)  
**Назначение:** backend для регистрации на мероприятия, админ-экспорта, CRUD мероприятий, галереи, уведомлений по email.  
**Документ:** инвентаризация текущего состояния (без рекомендаций и без изменений кода).

---

## 1. Общая информация о стеке

| Компонент | Версия / факт в проекте |
|-----------|-------------------------|
| **Java** | 21 (`pom.xml` → `<java.version>21</java.version>`; Docker base `eclipse-temurin:21-jre`) |
| **Spring Boot** | **3.3.2** (parent `spring-boot-starter-parent`) |
| **Maven** | Сборка через Maven (`pom.xml`); plugins: `maven-compiler-plugin`, `spring-boot-maven-plugin` |
| **PostgreSQL** | Driver runtime; локально БД `event_platform`; Docker image **`postgres:16`** |
| **Flyway** | `flyway-core` + `flyway-database-postgresql` (версии из BOM Boot 3.3.2); `spring.flyway.enabled: true` |
| **Hibernate / JPA** | Через `spring-boot-starter-data-jpa`; `ddl-auto: validate`; `open-in-view: false` |
| **Spring Data JPA** | `JpaRepository` для сущностей; кастомные derived/JPQL методы |
| **Spring Security** | `spring-boot-starter-security`; HTTP Basic; in-memory admin |
| **Spring Mail** | `spring-boot-starter-mail`; `JavaMailSender` + `SimpleMailMessage` |
| **Docker** | `Dockerfile` (JRE 21, jar); `docker-compose.yml` services `postgres` + `backend` |

### Зависимости `pom.xml` (все)

**Runtime / compile:**
- `org.springframework.boot:spring-boot-starter-web`
- `org.springframework.boot:spring-boot-starter-validation`
- `org.springframework.boot:spring-boot-starter-data-jpa`
- `org.springframework.boot:spring-boot-starter-security`
- `org.springframework.boot:spring-boot-starter-mail`
- `org.springframework.boot:spring-boot-starter-aop`
- `org.postgresql:postgresql` (scope `runtime`)
- `org.flywaydb:flyway-core`
- `org.flywaydb:flyway-database-postgresql`
- `org.projectlombok:lombok` **1.18.34** (optional)
- `org.mapstruct:mapstruct` **1.5.5.Final**
- `org.apache.poi:poi-ooxml` **5.2.5**

**Test:**
- `org.springframework.boot:spring-boot-starter-test` (scope `test`)

**Annotation processors (compiler):**
- `lombok` 1.18.34
- `mapstruct-processor` 1.5.5.Final

Отдельной зависимости `lombok-mapstruct-binding` в POM нет.

---

## 2. Структура проекта

```text
EventConstructor/
├── .env
├── .env.example
├── .gitignore
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── README.md
├── ARCHITECTURE.md
└── src/main/
    ├── java/com/example/eventplatform/
    │   ├── EventPlatformApplication.java
    │   ├── aspect/
    │   │   ├── LogEmailNotification.java
    │   │   ├── LogRegistration.java
    │   │   └── RegistrationLoggingAspect.java
    │   ├── config/
    │   │   ├── AdminProperties.java
    │   │   ├── CorsProperties.java
    │   │   ├── GalleryProperties.java
    │   │   ├── NotificationProperties.java
    │   │   └── WebConfig.java
    │   ├── controller/
    │   │   ├── AdminController.java
    │   │   ├── AdminEventController.java
    │   │   ├── EventController.java
    │   │   ├── GalleryController.java
    │   │   └── ParticipantRegistrationController.java
    │   ├── dto/
    │   │   ├── EventRequest.java
    │   │   ├── EventResponse.java
    │   │   ├── GalleryPhotoResponse.java
    │   │   └── ParticipantRegistrationRequest.java
    │   ├── entity/
    │   │   ├── AdminUser.java
    │   │   ├── Event.java
    │   │   ├── Participant.java
    │   │   └── ParticipantEvent.java
    │   ├── exception/
    │   │   ├── ApiErrorResponse.java
    │   │   ├── GlobalExceptionHandler.java
    │   │   └── ResourceNotFoundException.java
    │   ├── mapper/
    │   │   └── EventMapper.java
    │   ├── repository/
    │   │   ├── EventRepository.java
    │   │   ├── ParticipantEventRepository.java
    │   │   └── ParticipantRepository.java
    │   ├── security/
    │   │   └── SecurityConfig.java
    │   └── service/
    │       ├── AdminExportService.java / AdminExportServiceImpl.java
    │       ├── AdminParticipantService.java / AdminParticipantServiceImpl.java
    │       ├── EventAdminService.java / EventAdminServiceImpl.java
    │       ├── EventService.java / EventServiceImpl.java
    │       ├── GalleryService.java / GalleryServiceImpl.java
    │       ├── ParticipantRegistrationService.java / ParticipantRegistrationServiceImpl.java
    │       └── RegistrationNotificationService.java / RegistrationNotificationServiceImpl.java
    └── resources/
        ├── application.yml
        ├── application-docker.yml
        └── db/migration/
            ├── V1__create_participants.sql
            ├── V2__create_events.sql
            ├── V3__create_participant_events.sql
            ├── V4__create_admin_users.sql
            ├── V5__populate_event_dates.sql
            ├── V6__seed_events.sql
            ├── V7__add_registration_consents.sql
            └── V8__add_event_sort_order.sql
```

Каталог `src/test` отсутствует / тесты не обнаружены в структуре исходников. В пакетах есть пустые `.gitkeep`.

---

## 3. Архитектура

**Модель:** классический **Layered Architecture** в рамках **одного Spring Boot monolith** (не Modular Monolith с модулями Maven/Gradle, не DDD с bounded contexts).

**Слои (фактически):**

| Слой | Пакет | Роль |
|------|--------|------|
| API | `controller` | HTTP, binding, validation trigger (`@Valid`) |
| Application / Domain services | `service` (+ interfaces) | Бизнес-логика, транзакции |
| Persistence | `repository` | Spring Data JPA |
| Domain model (persistence) | `entity` | JPA entities |
| Transport contracts | `dto` | Request/Response; entities в REST не отдаются (кроме raw Excel bytes / Resource) |
| Mapping | `mapper` | MapStruct только для Event |
| Cross-cutting | `security`, `aspect`, `exception`, `config` | Auth, AOP-логи, errors, properties |

**Зависимости слоёв (направление «вниз»):**

```text
controller → service → repository → entity → PostgreSQL
                ↘ mapper (Event)
                ↘ config / mail / filesystem (Gallery)
aspect → service methods (по аннотациям)
security → config (AdminProperties); фильтрует HTTP до controller
```

**Не обнаружено:** отдельные Maven-модули; package-by-feature modules; явные Aggregate/Domain Service DDD; `AdminUserRepository` (entity `AdminUser` есть, в auth не используется).

---

## 4. Поток запроса

### Общий шаблон

```text
HTTP Request
  → SecurityFilterChain (CORS, CSRF off, Basic / permitAll)
  → DispatcherServlet
  → Controller (+ @Valid → MethodArgumentNotValidException при ошибке)
  → Service (@Transactional по месту)
  → Repository / Filesystem / JavaMailSender
  → Entity / DTO mapping
  → HTTP Response (DTO | Map | byte[] | Resource | 204)
```

### Регистрация `POST /api/participants/register`

```text
HTTP JSON body
  → ParticipantRegistrationController.register(@Valid ParticipantRegistrationRequest)
  → ParticipantRegistrationServiceImpl.register (@Transactional, @LogRegistration)
       → EventRepository.findAllById
       → validate existence → ResourceNotFoundException | continue
       → ParticipantRepository.findByEmail
            → update existing | build new Participant
       → ParticipantRepository.save
       → ParticipantEventRepository.findAllByParticipantId + saveAll missing links
       → RegistrationNotificationService.notifyNewRegistration (@LogEmailNotification)
            → если app.notification.enabled=false: aspect не вызывает send
            → иначе JavaMailSender.send(SimpleMailMessage)
            → ошибка mail ловится aspect’ом (registration уже сохранена)
  → ResponseEntity 200 { "participantId": UUID }
```

### Экспорт Excel `GET /api/admin/export`

```text
HTTP + Basic Auth
  → AdminController.exportParticipants(?eventIds)
  → AdminExportServiceImpl.exportParticipantsToExcel
       → ParticipantRepository.findAll | findDistinctBy...EventIdIn...
       → ParticipantEventRepository.findAllWithParticipantAndEvent (join fetch)
       → Apache POI XSSFWorkbook → byte[]
  → 200 application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
     Content-Disposition: participants-export.xlsx
```

### Список мероприятий `GET /api/events`

```text
HTTP (public)
  → EventController.getAvailableEvents
  → EventServiceImpl.getAvailableEvents (@Transactional readOnly)
       → EventRepository.findAllOrdered()
       → EventMapper.toResponse
  → 200 List<EventResponse>
```

**Факт:** метод репозитория `findAvailableOrdered()` (фильтр `registrationEnabled = true`) в `EventServiceImpl` **не вызывается**; вызывается `findAllOrdered()`.

### Админка мероприятий `/api/admin/events`

```text
HTTP + Basic Auth
  → AdminEventController
  → EventAdminServiceImpl
       → EventRepository + EventMapper
       → DELETE: ParticipantEventRepository.deleteAllByEventId затем EventRepository.deleteById
  → EventResponse / 201 / 204
```

---

## 5. Controllers

### `ParticipantRegistrationController` — `/api/participants`

| Method | Path | Request | Response | Validation | Service |
|--------|------|---------|----------|------------|---------|
| POST | `/register` | `ParticipantRegistrationRequest` JSON | `200` `Map<"participantId", UUID>` | `@Valid` на body | `ParticipantRegistrationService` |

### `EventController` — `/api/events`

| Method | Path | Request | Response | Validation | Service |
|--------|------|---------|----------|------------|---------|
| GET | `/` | — | `List<EventResponse>` | — | `EventService` |

### `GalleryController` — `/api/gallery`

| Method | Path | Request | Response | Validation | Service |
|--------|------|---------|----------|------------|---------|
| GET | `/` | — | `List<GalleryPhotoResponse>` | — | `GalleryService` |
| GET | `/{filename}` | path `filename` | `Resource` + Cache-Control + MediaType | filename проверяется в service | `GalleryService` |

### `AdminController` — `/api/admin`

| Method | Path | Request | Response | Validation | Service |
|--------|------|---------|----------|------------|---------|
| GET | `/export` | query `eventIds` optional `List<UUID>` | `byte[]` Excel | — | `AdminExportService` |
| DELETE | `/participants` | — | `200` `{ deletedCount: long }` | — | `AdminParticipantService` |

### `AdminEventController` — `/api/admin/events`

| Method | Path | Request | Response | Validation | Service |
|--------|------|---------|----------|------------|---------|
| GET | `/` | — | `List<EventResponse>` | — | `EventAdminService` |
| GET | `/{id}` | UUID | `EventResponse` | — | … |
| POST | `/` | `EventRequest` | `201 EventResponse` | `@Valid` | … |
| PUT | `/{id}` | `EventRequest` | `EventResponse` | `@Valid` | … |
| DELETE | `/{id}` | UUID | `204 No Content` | — | … |

---

## 6. Services

| Interface / Impl | Назначение | Repositories / deps | Entities / др. | Бизнес-логика (факт) |
|------------------|------------|---------------------|----------------|----------------------|
| `ParticipantRegistrationServiceImpl` | Регистрация | `ParticipantRepository`, `EventRepository`, `ParticipantEventRepository`, `RegistrationNotificationService` | Participant, Event, ParticipantEvent | Create-or-update по email; добавить недостающие связи participant↔event (старые не удаляет); уведомить email |
| `RegistrationNotificationServiceImpl` | Письмо подтверждения | `JavaMailSender`, `NotificationProperties` | Participant, Event | Subject/body по заявке; `from` = notification.from |
| `EventServiceImpl` | Публичный список событий | `EventRepository`, `EventMapper` | Event | `findAllOrdered` → DTO |
| `EventAdminServiceImpl` | CRUD событий | `EventRepository`, `ParticipantEventRepository`, `EventMapper` | Event | CRUD; delete чистит связи по eventId |
| `AdminExportServiceImpl` | Excel участников | `ParticipantRepository`, `ParticipantEventRepository` | Participant, ParticipantEvent | Фильтр по eventIds или все; колонки RU + Да/Нет |
| `AdminParticipantServiceImpl` | Удалить всех участников | оба participant repos | — | count → deleteAllInBatch links → participants |
| `GalleryServiceImpl` | Файловая галерея | `GalleryProperties` | — | list/get jpg/jpeg/png/webp/gif из storage path; path traversal защита |

Interfaces всегда есть, реализация одна.

---

## 7. Repository

### `ParticipantRepository extends JpaRepository<Participant, UUID>`
- `Optional<Participant> findByEmail(String email)`
- `List<Participant> findDistinctByParticipantEventsEventIdInOrderByCreatedAtAsc(Collection<UUID> eventIds)`
- **Paging / Sorting API / Specification:** не используются как типы Spring Data paging.

### `EventRepository extends JpaRepository<Event, UUID>`
- `@Query` JPQL `findAllOrdered()` — CASE null sortOrder, затем sortOrder, eventDate
- `@Query` JPQL `findAvailableOrdered()` — то же + `registrationEnabled = true` (**в текущем публичном сервисе не вызван**)

### `ParticipantEventRepository extends JpaRepository<ParticipantEvent, UUID>`
- `findAllByParticipantId`
- `existsByParticipantIdAndEventId` (в registration-потоке не используется сервисом)
- `deleteAllByEventId`
- `@Query` + `join fetch` participant и event: `findAllWithParticipantAndEvent`

**Отсутствует:** `AdminUserRepository`.

---

## 8. Entity

### `Participant` → `participants`
- Lombok: `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(onlyExplicitlyIncluded=true)`
- ID: `UUID`, `@GeneratedValue` + `@UuidGenerator`, в equals только `id`
- Поля: firstName, lastName, email (unique), company, projectRole, stack, grade, telegram, personalDataConsent, photoConsent, newsletterConsent
- `OneToMany` → `ParticipantEvent` (`mappedBy="participant"`, `cascade=ALL`, `orphanRemoval=false`, default `HashSet`, `@Builder.Default`)
- `@CreationTimestamp createdAt` (updatable=false), `@UpdateTimestamp updatedAt`
- Fetch на OneToMany: **default LAZY** (JPA default для OneToMany)

### `Event` → `events`
- Аналогичный Lombok/UUID/equals
- Поля: title, description (TEXT), eventDate, companyName, location, registrationEnabled (`boolean`), sortOrder (`Integer`)
- `OneToMany` → `ParticipantEvent` (`cascade=ALL`, `orphanRemoval=false`)
- timestamps как у Participant

### `ParticipantEvent` → `participant_events`
- UniqueConstraint `uk_participant_events_participant_event` на `(participant_id, event_id)`
- `ManyToOne` Participant **LAZY** optional=false
- `ManyToOne` Event **LAZY** optional=false
- `@CreationTimestamp createdAt` (нет UpdateTimestamp)

### `AdminUser` → `admin_users`
- UUID id, username unique, passwordHash, createdAt
- **Не участвует** в `SecurityConfig` / нет repository

**ManyToMany:** нет явной `@ManyToMany`; связь N:M через `ParticipantEvent`.

---

## 9. DTO

| DTO | Роль | Поля |
|-----|------|------|
| `ParticipantRegistrationRequest` | Request | firstName, lastName, email, company, projectRole, stack, grade, telegram, eventIds, 3× consent booleans |
| `EventRequest` | Request (admin) | title, description, eventDate, registrationEnabled, sortOrder |
| `EventResponse` | Response (public + admin) | id, title, description, eventDate, companyName, location, registrationEnabled, sortOrder, createdAt, updatedAt |
| `GalleryPhotoResponse` | Response | filename, url |
| Inline `Map` | Response | `participantId`; `deletedCount` |
| Excel / Resource | Response | не DTO |

Entity в API-ответах JSON не используются.

---

## 10. Validation

Jakarta Validation на request DTO с `@Valid` в controllers:

**`ParticipantRegistrationRequest`:**  
`@NotBlank` firstName, lastName, company, projectRole, stack, grade; `@Email @NotBlank` email; `@NotEmpty List<@NotNull UUID> eventIds`; telegram без обязательности; consents — примитивы `boolean` без `@AssertTrue`.

**`EventRequest`:**  
`@NotBlank` title; `@NotNull` eventDate; `@NotNull Boolean registrationEnabled`; description и sortOrder необязательны.

Ошибки → `MethodArgumentNotValidException` → 400 через `GlobalExceptionHandler`.

---

## 11. Exception Handling

**`ResourceNotFoundException`** — runtime, message string.  
**`GlobalExceptionHandler` `@RestControllerAdvice`:**

| Exception | Status | Body |
|-----------|--------|------|
| `ResourceNotFoundException` | 404 | `ApiErrorResponse` message = ex message, details=[] |
| `MethodArgumentNotValidException` | 400 | message=`Validation failed`, details=`field: msg` |
| `Exception` | 500 | message=`Unexpected server error`, details=[ex.getMessage()] |

**`ApiErrorResponse` record:** timestamp, status, error (reason phrase), message, details.

Не перехватываются отдельно: `MailSendException`, security 401 (обрабатывает Spring Security), `IllegalStateException` из export/gallery → попадает в generic 500.

---

## 12. Security

Файл: `SecurityConfig`.

- **CSRF:** disabled
- **CORS:** `http.cors(Customizer.withDefaults())` + `WebConfig` mapping `/api/**`
- **HTTP Basic:** enabled
- **UserDetailsService:** `InMemoryUserDetailsManager`, один user из `AdminProperties`, password через `PasswordEncoderFactories.createDelegatingPasswordEncoder()` (encode при создании bean), role `ADMIN`
- **AuthenticationManager:** явного `@Bean` нет — стандартная автоконфигурация Spring Security от `UserDetailsService`

**Правила authorize:**

| Matcher | Access |
|---------|--------|
| GET `/api/events`, `/api/events/**` | permitAll |
| GET `/api/gallery`, `/api/gallery/**` | permitAll |
| POST `/api/participants/register` | permitAll |
| `/api/admin/**` | authenticated |
| anyRequest | permitAll |

Таблица `admin_users` / entity `AdminUser` **не подключены** к authentication.

Неуспешная Basic Auth на `/api/admin/**` → **401** (поведение Spring Security).

---

## 13. Mail

- Starter: `spring-boot-starter-mail`
- Config `spring.mail.*` из env: HOST/PORT/USERNAME/PASSWORD; SMTP auth + starttls
- App: `app.notification.enabled` ← `MAIL_ENABLED`; `app.notification.from` ← `MAIL_USERNAME`
- Реализация: `RegistrationNotificationServiceImpl` + `JavaMailSender` + `SimpleMailMessage`
- Gate отправки: aspect `@LogEmailNotification` — если `enabled=false`, `proceed()` не вызывается
- При ошибке SMTP: aspect логирует ERROR и **проглатывает** исключение → HTTP регистрации всё равно 200 (если БД save уже прошёл в той же транзакции; mail внутри той же `@Transactional` регистрации — aspect ловит Throwable вокруг `proceed()`, поэтому исключение из send **не пробрасывается** наружу из aspect-метода `void logEmailNotification`)

Письмо: to = email участника; subject «Ваша заявка принята…»; body с полями заявки и списком мероприятий.

`newsletterConsent` на отправку подтверждения **не влияет**.

---

## 14. Flyway

| Migration | Назначение |
|-----------|------------|
| V1 | CREATE `participants` |
| V2 | CREATE `events` |
| V3 | CREATE `participant_events` + FKs + UNIQUE (participant_id, event_id) |
| V4 | CREATE `admin_users` |
| V5 | UPDATE dates/registration_enabled для известных UUID / всех |
| V6 | INSERT seed 2 events ON CONFLICT DO NOTHING |
| V7 | ADD consent columns на participants NOT NULL DEFAULT FALSE |
| V8 | ADD `sort_order INTEGER NULL` на events |

Locations: `classpath:db/migration`. История: таблица Flyway по умолчанию `flyway_schema_history`.

---

## 15. PostgreSQL (схема после V1–V8)

### `participants`
PK `id UUID`  
UNIQUE `email`  
NOT NULL: first_name, last_name, email, company, project_role, stack, personal_data_consent, photo_consent, newsletter_consent  
Nullable: grade, telegram, created_at, updated_at

### `events`
PK `id UUID`  
NOT NULL: title  
Nullable: description, event_date, company_name, location, registration_enabled, sort_order, timestamps

### `participant_events`
PK `id`  
FK `participant_id` → participants(id)  
FK `event_id` → events(id)  
UNIQUE (participant_id, event_id)  
Nullable: created_at

### `admin_users`
PK `id`  
UNIQUE username  
Nullable: password_hash, created_at

**Отдельные INDEX** (кроме PK/UNIQUE) в миграциях **не создавались**.

---

## 16. Docker

**Dockerfile:** `FROM eclipse-temurin:21-jre`; `COPY target/*.jar app.jar`; ENTRYPOINT `java -jar`; EXPOSE 8080. Сборка jar **вне** image (multi-stage нет).

**docker-compose:**
- **postgres:** image 16, DB/user/pass `event_platform`/`postgres`/`postgres`, port 5432, volume `postgres_data`, healthcheck `pg_isready`
- **backend:** build Dockerfile, depends_on healthy postgres, profile `docker`, env DB_*, MAIL_*, GALLERY_STORAGE_PATH, CORS_*, port 8080, bind mount `./data/gallery` → `/app/data/gallery`
- Default network compose (неименованная явная network секция отсутствует)

---

## 17. Конфигурация

### `application.yml`
- optional import `.env[.properties]`
- datasource localhost `event_platform` / `event_app` / `event_app_password`
- JPA validate, format_sql, open-in-view false
- Flyway on
- Mail NIC defaults
- server 8080
- logging hibernate SQL info
- `app.admin` username/password **admin/admin**
- notification, gallery `./data/gallery`, cors origins 5173/3000/3001

### `application-docker.yml`
- datasource через DB_HOST/PORT/NAME/USERNAME/PASSWORD
- mail/env override
- gallery `/app/data/gallery`
- cors default **без** `3001` (override через env compose с 3001)

### `@ConfigurationProperties`
- `AdminProperties` `app.admin`
- `NotificationProperties` `app.notification`
- `GalleryProperties` `app.gallery`
- `CorsProperties` `app.cors`  
Включены в `EventPlatformApplication` через `@EnableConfigurationProperties`.

### Beans
- `SecurityFilterChain`, `UserDetailsService`, `PasswordEncoder`
- MapStruct `EventMapper` (componentModel spring)
- Services/Controllers/Aspects/WebConfig — stereotype/component

---

## 18. REST API (сводка)

| Method | URL | Auth | Request | Response |
|--------|-----|------|---------|----------|
| POST | `/api/participants/register` | no | ParticipantRegistrationRequest | `{participantId}` |
| GET | `/api/events` | no | — | EventResponse[] |
| GET | `/api/gallery` | no | — | GalleryPhotoResponse[] |
| GET | `/api/gallery/{filename}` | no | — | image Resource |
| GET | `/api/admin/export` | Basic | `eventIds?` | xlsx bytes |
| DELETE | `/api/admin/participants` | Basic | — | `{deletedCount}` |
| GET | `/api/admin/events` | Basic | — | EventResponse[] |
| GET | `/api/admin/events/{id}` | Basic | — | EventResponse |
| POST | `/api/admin/events` | Basic | EventRequest | 201 EventResponse |
| PUT | `/api/admin/events/{id}` | Basic | EventRequest | EventResponse |
| DELETE | `/api/admin/events/{id}` | Basic | — | 204 |

---

## 19. Логирование

- **AOP:** `RegistrationLoggingAspect` (`@Slf4j`)
  - `@LogRegistration` на `register`: start/complete/fail + duration
  - `@LogEmailNotification` на `notifyNewRegistration`: skip/start/sent/fail + duration
- **Logger:** SLF4J via Lombok; нет кастомных MVC Interceptor
- **application.yml:** `org.hibernate.SQL: info`

Аннотации-маркеры: `LogRegistration`, `LogEmailNotification` (`@Retention RUNTIME`, method).

---

## 20. Mapper (MapStruct)

**`EventMapper`** `@Mapper(componentModel = "spring")`:
- `Event → EventResponse` (все matching поля)
- `EventRequest → Event` (ignore: id, companyName, location, participantEvents, createdAt, updatedAt)
- `updateEntity(Event, EventRequest)` с теми же ignore

Других mapper нет (регистрация маппится вручную в service).

---

## 21. Архитектурная схема (ASCII)

```text
                    ┌──────────────┐
                    │   Clients    │
                    │ Front/Admin  │
                    └──────┬───────┘
                           │ HTTP :8080
                    ┌──────▼───────┐
                    │Spring Security│ Basic / permitAll
                    │ + CORS        │
                    └──────┬───────┘
                           │
          ┌────────────────┼────────────────┐
          ▼                ▼                ▼
   Public Controllers  Admin Controllers  Gallery
          │                │                │
          ▼                ▼                ▼
        Services ──────────┴──── Aspects (mail/reg log)
          │
    ┌─────┼──────┬──────────┐
    ▼     ▼      ▼          ▼
  Repos Mapper Filesystem  JavaMailSender
    │
    ▼
 Hibernate / Flyway-validated schema
    │
    ▼
 PostgreSQL (event_platform)
```

---

## 22. Диаграмма зависимостей пакетов

```text
EventPlatformApplication
        │ enables
        ▼
     config ◄── security, WebConfig, services(mail/gallery)
        ▲
controller ──► service ──► repository ──► entity
        │          │
        │          ├──► mapper (Event)
        │          ├──► aspect (annotations on service)
        │          └──► exception (throws)
        └──► dto
exception (advice) ──► all controllers (implicit)
```

---

## 23. Библиотеки — где и для чего

| Библиотека | Где | Для чего |
|------------|-----|----------|
| Spring Boot | весь app | runtime |
| Spring Web | controllers | REST |
| Validation | DTO + `@Valid` | входная валидация |
| Spring Data JPA / Hibernate | repos/entities | ORM |
| Flyway | migrations | схема БД |
| Spring Security | SecurityConfig | Basic admin |
| Spring Mail | RegistrationNotificationServiceImpl | SMTP |
| Spring AOP | RegistrationLoggingAspect | логи регистрации/почты |
| MapStruct | EventMapper | Event DTO↔entity |
| Apache POI | AdminExportServiceImpl | XLSX |
| Lombok | entities/services/controllers | boilerplate |
| PostgreSQL driver | datasource | JDBC |
| Docker/Compose | deploy | postgres+backend |

---

## 24. Производительность (только факты текущего кода)

| Тема | Состояние |
|------|-----------|
| **open-in-view** | `false` |
| **Fetch** | ManyToOne LAZY; OneToMany default LAZY; export использует `join fetch` |
| **Транзакции** | `@Transactional` на register, CRUD events, delete all, export(readOnly), public events(readOnly) |
| **Optional** | `findByEmail`, `findById` в admin get |
| **Batch** | `deleteAllInBatch` при wipe участников; `saveAll` связей |
| **Paging** | нет |
| **Cache** | нет (Spring Cache / 2nd level не подключены) |
| **Индексы** | только PK/UNIQUE из миграций |
| **N+1 риск** | `addMissingEventLinks`: `findAllByParticipantId` без fetch event, затем `getEvent().getId()` — потенциальные lazy loads; gallery list — filesystem stream |
| **Eager списки** | `findAll()` участников при полном export |

---

## 25. Итоговая техническая карта

**Тип системы:** одномодульный Spring Boot 3.3.2 / Java 21 REST backend поверх PostgreSQL с Flyway-схемой.

**Доменные области в одном приложении:**
1. Регистрация участников + связи с мероприятиями + email confirmation
2. Публичный каталог мероприятий + файловая галерея
3. Админ: Basic Auth, Excel export (multi eventIds), wipe participants, CRUD events

**Граница ответственности персистенции:** Hibernate только `validate`; схема и данные seed — Flyway V1–V8.

**Контракты API:** Java records DTO; JSON; Excel binary; gallery binary Resource.

**Auth модель:** один in-memory admin из `application.yml` / env; таблица `admin_users` существует в БД, но **не используется** runtime-аутентификацией.

**Наблюдаемость:** AOP-логи регистрации и почты; SQL log level info.

**Деплой:** локальный Maven jar + Compose (prebuilt jar copy into Temurin 21 JRE).

---

*Конец инвентаризации. Изменения в код и рекомендации не входят в объём документа.*

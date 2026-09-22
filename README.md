# Financial Control

A personal finance management REST API built with Spring Boot. It lets users manage accounts,
record transactions and transfers, and receive email notifications for important events, backed
by JWT authentication, PostgreSQL persistence, and Kafka-based asynchronous messaging.

## Features

- **Authentication & Security** — JWT-based login/registration, Argon2 (BouncyCastle) password
  hashing with an additional pepper, rate limiting on auth endpoints (Bucket4j), and CORS
  configuration.
- **Accounts** — Create, update, delete, and list financial accounts.
- **Transactions** — Create, update, delete, list, and confirm transactions.
- **Transfers** — Create, update, delete, and list transfers between accounts.
- **User Management** — Update profile, change password, delete account, view current user.
- **Email Notifications** — Pluggable email providers (SMTP, Resend, Mailtrap) triggered via
  Kafka events using the transactional outbox pattern.
- **API Documentation** — OpenAPI/Swagger UI available out of the box.

## Tech Stack

| Category            | Technology                                    |
|----------------------|-----------------------------------------------|
| Language / Runtime   | Java 25                                       |
| Framework            | Spring Boot 4.1.1 (Web MVC, Validation, Data JPA, Security, Mail, Kafka) |
| Database             | PostgreSQL 17                                 |
| Messaging            | Apache Kafka                                  |
| Authentication       | JJWT (JSON Web Tokens)                        |
| Password Hashing     | BouncyCastle (Argon2) + pepper                |
| Rate Limiting        | Bucket4j                                      |
| Object Mapping       | MapStruct                                     |
| API Docs             | springdoc-openapi (Swagger UI)                |
| Build Tool           | Maven (Maven Wrapper included)                |

## Project Structure

```
src/main/java/com/cruz/financialcontrol/
├── config/          # Application and OpenAPI configuration
├── controller/       # REST controllers (Auth, User, Account, Transaction, Transfer)
├── exception/         # Exceptions and global exception handlers
├── messaging/         # Kafka topics, outbox relay/publisher, email listener
├── model/
│   ├── dto/            # Request/response DTOs per domain
│   ├── entity/          # JPA entities
│   ├── enums/            # Enums (transaction status/type, outbox status)
│   ├── event/             # Domain/event payloads
│   └── mapper/             # MapStruct mappers
├── repository/         # Spring Data JPA repositories
├── security/            # JWT filter/service, security config, password encoding, rate limiting
└── service/              # Business logic and email provider implementations
```

## Prerequisites

- JDK 25
- Maven (or use the included `mvnw`/`mvnw.cmd` wrapper)
- Docker & Docker Compose (for PostgreSQL, Kafka, and Kafka UI)

## Getting Started

### 1. Start infrastructure

```powershell
docker compose up -d
```

This starts:
- **PostgreSQL** on `localhost:5432` (db: `financial_control`)
- **Kafka** on `localhost:9092`
- **Kafka UI** on `localhost:8090`

### 2. Configure environment variables

Create/update the `.env` file (or export the variables in your shell) with values such as:

```
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

SECURITY_JWT_SECRET=<hex-encoded secret>
SECURITY_JWT_EXPIRATION=86400
SECURITY_PASSWORD_PEPPER=<base64 pepper>

EMAIL_PROVIDER=mailtrap   # smtp | resend | mailtrap
RESEND_API_TOKEN=...
MAILTRAP_API_TOKEN=...
MAILTRAP_API_INBOXID=...
```

### 3. Run the application

```powershell
.\mvnw.cmd spring-boot:run
```

The API will be available at `http://localhost:8080/api/v1`.

### 4. API Documentation

Once running, Swagger UI is available at:

```
http://localhost:8080/api/v1/swagger-ui.html
```

## API Overview

All endpoints are prefixed with `/api/v1`.

| Resource     | Base path       | Operations                                              |
|--------------|-----------------|----------------------------------------------------------|
| Auth         | `/auth`         | `POST /login`, `POST /register`                          |
| Users        | `/users`        | `GET /me`, `PUT /me`, `POST /me/password`, `DELETE /me`   |
| Accounts     | `/accounts`     | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`   |
| Transactions | `/transactions` | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`, `PATCH /{id}/confirm` |
| Transfers    | `/transfers`    | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`   |

## Configuration

Main application settings live in `src/main/resources/application.yaml`, including:

- **Datasource** — PostgreSQL connection (JPA `ddl-auto: update`, to be replaced by Flyway later).
- **Mail** — SMTP host/port with pluggable provider selection (`app.email.provider`).
- **Kafka** — Producer/consumer configuration, consumer group `email-notifications`.
- **Security** — JWT secret/expiration, password pepper, and CORS rules.
- **Springdoc** — OpenAPI/Swagger UI paths and behavior.

## Testing

Run the test suite with:

```powershell
.\mvnw.cmd test
```

Tests use `spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test`, and Mockito.

## License

This project is licensed under the **Creative Commons Attribution-NonCommercial 4.0
International** license. Visualization and non-commercial use are permitted; commercial use is
strictly prohibited.

## Author

**Edson Cruz** — edson.l.cruz@gmail.com

# JP Microservices Platform

## Complete README.md

```markdown
# JP Microservices Platform

A production-grade microservices architecture built with Java 17, Spring Boot 3.5.13, and Spring Cloud 2025.0.2, 
following JPMC engineering standards.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Tech Stack](#tech-stack)
3. [Project Structure](#project-structure)
4. [Prerequisites](#prerequisites)
5. [Database Setup](#database-setup)
6. [Configuration](#configuration)
7. [Running the Platform](#running-the-platform)
8. [API Reference](#api-reference)
9. [Testing](#testing)
10. [Troubleshooting](#troubleshooting)
11. [Roadmap](#roadmap)

---

## Architecture Overview

```
Client Request
      │
      ▼
┌─────────────────┐
│   API Gateway   │  :8080  - Single entry point, routing, load balancing
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Eureka Server  │  :8761  - Service discovery & registration
└─────────────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌────────┐ ┌─────────────┐
│  User  │ │ Transaction │
│Service │ │   Service   │
│ :8081  │ │   :8082     │
└───┬────┘ └──────┬──────┘
    │              │
    ▼              ▼
┌────────┐ ┌─────────────┐
│ userdb │ │transaction  │
│ :5432  │ │    db       │
│(PG 15) │ │   :5433     │
└────────┘ │  (PG 15)    │
           └─────────────┘
```

### Design Principles

- **Database-per-service** — Each microservice owns its data store (no shared schemas)
- **API Gateway pattern** — All external traffic enters through a single point
- **Service discovery** — Services register with Eureka; Gateway resolves by service name
- **Config Server** — Centralised configuration management (port 8888)
- **Externalized configuration** — Environment-specific configs, never hardcoded secrets

---

## Tech Stack

| Layer              | Technology                        | Version       |
|--------------------|-----------------------------------|---------------|
| Language           | Java                              | 17            |
| Framework          | Spring Boot                       | 3.5.13        |
| Cloud              | Spring Cloud                      | 2025.0.2      |
| Service Discovery  | Netflix Eureka                    | Spring Cloud  |
| API Gateway        | Spring Cloud Gateway              | Spring Cloud  |
| Config Management  | Spring Cloud Config Server        | Spring Cloud  |
| Database           | PostgreSQL                        | 15            |
| ORM                | Spring Data JPA / Hibernate       | Spring Boot   |
| Build Tool         | Gradle                            | 8.x           |
| Java Runtime       | OpenJDK / Eclipse Temurin         | 17 LTS        |

---

## Project Structure

```
jp-microservices/
├── config-server/                  # Centralised config server (8888)
│   ├── src/main/java/
│   │   └── com/jp/configserver/
│   │       └── ConfigServerApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── build.gradle
│
├── eureka-server/                  # Service discovery (8761)
│   ├── src/main/java/
│   │   └── com/jp/eurekaserver/
│   │       └── EurekaServerApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── build.gradle
│
├── api-gateway/                    # API Gateway (8080)
│   ├── src/main/java/
│   │   └── com/jp/apigateway/
│   │       └── ApiGatewayApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── build.gradle
│
├── user-service/                   # User management (8081)
│   ├── src/main/java/
│   │   └── com/jp/userservice/
│   │       ├── UserServiceApplication.java
│   │       ├── controller/
│   │       │   └── UserController.java
│   │       ├── service/
│   │       │   └── UserService.java
│   │       ├── repository/
│   │       │   └── UserRepository.java
│   │       ├── model/
│   │       │   └── User.java
│   │       └── dto/
│   │           ├── UserRequest.java
│   │           └── UserResponse.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── build.gradle
│
├── transaction-service/            # Transaction management (8082)
│   ├── src/main/java/
│   │   └── com/jp/transactionservice/
│   │       ├── TransactionServiceApplication.java
│   │       ├── controller/
│   │       │   └── TransactionController.java
│   │       ├── service/
│   │       │   └── TransactionService.java
│   │       ├── repository/
│   │       │   └── TransactionRepository.java
│   │       ├── model/
│   │       │   └── Transaction.java
│   │       └── dto/
│   │           ├── TransactionRequest.java
│   │           └── TransactionResponse.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── build.gradle
│
├── .gitignore
└── README.md
```

---

## Prerequisites

Ensure the following are installed before running the platform:

| Tool        | Version   | Download                              |
|-------------|-----------|---------------------------------------|
| Java (JDK)  | 17 LTS    | https://adoptium.net                  |
| PostgreSQL  | 15        | https://www.postgresql.org/download   |
| Gradle      | 8.x       | https://gradle.org/releases           |
| Git         | Latest    | https://git-scm.com                   |

### Verify installations

```bash
java -version
# openjdk version "17.x.x"

psql --version
# psql (PostgreSQL) 15.x

gradle --version
# Gradle 8.x
```

---

## Database Setup

### Step 1 — Create databases and users

Connect to PostgreSQL as a superuser:

```bash
psql -U postgres
```

Run the following:

```sql
-- ============================================================
-- USER SERVICE DATABASE
-- ============================================================
CREATE DATABASE userdb;
CREATE USER userservice WITH ENCRYPTED PASSWORD 'userpass';
GRANT ALL PRIVILEGES ON DATABASE userdb TO userservice;

-- ============================================================
-- TRANSACTION SERVICE DATABASE
-- ============================================================
CREATE DATABASE transactiondb;
CREATE USER transactionservice WITH ENCRYPTED PASSWORD 'transactionpass';
GRANT ALL PRIVILEGES ON DATABASE transactiondb TO transactionservice;
```

### Step 2 — Create schemas

#### User DB Schema

Connect to userdb:

```bash
psql -U postgres -d userdb
```

```sql
-- Grant schema permissions
GRANT ALL ON SCHEMA public TO userservice;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    first_name  VARCHAR(50)  NOT NULL,
    last_name   VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_users_email    ON users(email);
CREATE INDEX idx_users_username ON users(username);
```

#### Transaction DB Schema

Connect to transactiondb:

```bash
psql -U postgres -d transactiondb
```

```sql
-- Grant schema permissions
GRANT ALL ON SCHEMA public TO transactionservice;

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id              BIGSERIAL    PRIMARY KEY,
    transaction_ref VARCHAR(50)  NOT NULL UNIQUE,
    sender_id       BIGINT       NOT NULL,
    receiver_id     BIGINT       NOT NULL,
    amount          DECIMAL(19,4) NOT NULL,
    currency        VARCHAR(3)   NOT NULL DEFAULT 'GBP',
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    description     VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_transactions_sender_id       ON transactions(sender_id);
CREATE INDEX idx_transactions_receiver_id     ON transactions(receiver_id);
CREATE INDEX idx_transactions_status          ON transactions(status);
CREATE INDEX idx_transactions_transaction_ref ON transactions(transaction_ref);
```

### Step 3 — Configure separate PostgreSQL instances (optional)

If running Transaction DB on port 5433 (recommended for isolation):

```bash
# macOS (Homebrew)
brew install postgresql@15
pg_lsclusters                        # List clusters (Linux)
pg_createcluster 15 transaction      # Create second cluster (Linux)
pg_ctlcluster 15 transaction start   # Start second cluster (Linux)

# Or use Docker
docker run --name userdb \
  -e POSTGRES_DB=userdb \
  -e POSTGRES_USER=userservice \
  -e POSTGRES_PASSWORD=userpass \
  -p 5432:5432 -d postgres:15

docker run --name transactiondb \
  -e POSTGRES_DB=transactiondb \
  -e POSTGRES_USER=transactionservice \
  -e POSTGRES_PASSWORD=transactionpass \
  -p 5433:5432 -d postgres:15
```

---

## Configuration

### Config Server (8888)

`config-server/src/main/resources/application.yml`

```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/config
```

### Eureka Server (8761)

`eureka-server/src/main/resources/application.yml`

```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### API Gateway (8080)

`api-gateway/src/main/resources/application.yml`

```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
        - id: transaction-service
          uri: lb://transaction-service
          predicates:
            - Path=/api/transactions/**

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### User Service (8081)

`user-service/src/main/resources/application.yml`

```yaml
server:
  port: 8081

spring:
  application:
    name: user-service
  datasource:
    url: jdbc:postgresql://localhost:5432/userdb
    username: userservice
    password: userpass
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Transaction Service (8082)

`transaction-service/src/main/resources/application.yml`

```yaml
server:
  port: 8082

spring:
  application:
    name: transaction-service
  datasource:
    url: jdbc:postgresql://localhost:5433/transactiondb
    username: transactionservice
    password: transactionpass
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

---

## Running the Platform

> ⚠️ **Critical** — Services MUST be started in this exact order.
> Skipping or reordering will cause registration and routing failures.

### Startup Order

```
1. Config Server   (8888)
2. Eureka Server   (8761)
3. API Gateway     (8080)
4. User Service    (8081)
5. Transaction Service (8082)
```

### Start each service

Open a separate terminal for each service:

```bash
# Terminal 1 — Config Server
cd config-server
./gradlew bootRun

# Terminal 2 — Eureka Server (wait for Config Server to be healthy)
cd eureka-server
./gradlew bootRun

# Terminal 3 — API Gateway (wait for Eureka to be healthy)
cd api-gateway
./gradlew bootRun

# Terminal 4 — User Service
cd user-service
./gradlew bootRun

# Terminal 5 — Transaction Service
cd transaction-service
./gradlew bootRun
```

### Verify all services are running

```bash
# Eureka dashboard — all services should appear here
open http://localhost:8761

# Config Server health
curl http://localhost:8888/actuator/health

# API Gateway health
curl http://localhost:8080/actuator/health

# User Service health (direct)
curl http://localhost:8081/actuator/health

# Transaction Service health (direct)
curl http://localhost:8082/actuator/health
```

---

## API Reference

All requests should go through the **API Gateway on port 8080**.  
Direct service ports (8081, 8082) are for internal/debug use only.

### Base URL

```
http://localhost:8080
```

---

### User Service — `/api/users`

#### Create User

```http
POST /api/users
Content-Type: application/json

{
  "username": "jsmith",
  "email": "j.smith@jpmc.com",
  "firstName": "John",
  "lastName": "Smith"
}
```

**Response — 201 Created**

```json
{
  "id": 1,
  "username": "jsmith",
  "email": "j.smith@jpmc.com",
  "firstName": "John",
  "lastName": "Smith",
  "createdAt": "2025-01-15T09:30:00",
  "updatedAt": "2025-01-15T09:30:00"
}
```

---

#### Get All Users

```http
GET /api/users
```

**Response — 200 OK**

```json
[
  {
    "id": 1,
    "username": "jsmith",
    "email": "j.smith@jpmc.com",
    "firstName": "John",
    "lastName": "Smith",
    "createdAt": "2025-01-15T09:30:00",
    "updatedAt": "2025-01-15T09:30:00"
  }
]
```

---

#### Get User by ID

```http
GET /api/users/{id}
```

**Response — 200 OK**

```json
{
  "id": 1,
  "username": "jsmith",
  "email": "j.smith@jpmc.com",
  "firstName": "John",
  "lastName": "Smith",
  "createdAt": "2025-01-15T09:30:00",
  "updatedAt": "2025-01-15T09:30:00"
}
```

---

#### Update User

```http
PUT /api/users/{id}
Content-Type: application/json

{
  "username": "jsmith",
  "email": "john.smith@jpmc.com",
  "firstName": "John",
  "lastName": "Smith"
}
```

**Response — 200 OK**

```json
{
  "id": 1,
  "username": "jsmith",
  "email": "john.smith@jpmc.com",
  "firstName": "John",
  "lastName": "Smith",
  "createdAt": "2025-01-15T09:30:00",
  "updatedAt": "2025-01-15T10:15:00"
}
```

---

#### Delete User

```http
DELETE /api/users/{id}
```

**Response — 204 No Content**

---

### Transaction Service — `/api/transactions`

#### Transaction Status Values

| Status      | Description                              |
|-------------|------------------------------------------|
| `PENDING`   | Default — transaction awaiting processing |
| `COMPLETED` | Successfully processed                    |
| `FAILED`    | Processing failed                         |
| `CANCELLED` | Cancelled before processing               |
| `REVERSED`  | Completed but subsequently reversed       |

---

#### Create Transaction

```http
POST /api/transactions
Content-Type: application/json

{
  "senderId": 1,
  "receiverId": 2,
  "amount": 1500.00,
  "currency": "GBP",
  "description": "Invoice payment Q1"
}
```

**Response — 201 Created**

```json
{
  "id": 1,
  "transactionRef": "TXN-EA05201F-D04",
  "senderId": 1,
  "receiverId": 2,
  "amount": 1500.0000,
  "currency": "GBP",
  "status": "PENDING",
  "description": "Invoice payment Q1",
  "createdAt": "2025-01-15T09:30:00",
  "updatedAt": "2025-01-15T09:30:00"
}
```

---

#### Get All Transactions

```http
GET /api/transactions
```

**Response — 200 OK**

```json
[
  {
    "id": 1,
    "transactionRef": "TXN-EA05201F-D04",
    "senderId": 1,
    "receiverId": 2,
    "amount": 1500.0000,
    "currency": "GBP",
    "status": "PENDING",
    "description": "Invoice payment Q1",
    "createdAt": "2025-01-15T09:30:00",
    "updatedAt": "2025-01-15T09:30:00"
  }
]
```

---

#### Get Transaction by ID

```http
GET /api/transactions/{id}
```

**Response — 200 OK**

```json
{
  "id": 1,
  "transactionRef": "TXN-EA05201F-D04",
  "senderId": 1,
  "receiverId": 2,
  "amount": 1500.0000,
  "currency": "GBP",
  "status": "PENDING",
  "description": "Invoice payment Q1",
  "createdAt": "2025-01-15T09:30:00",
  "updatedAt": "2025-01-15T09:30:00"
}
```

---

#### Get Transaction by Reference

```http
GET /api/transactions/ref/{transactionRef}
```

**Example:**

```http
GET /api/transactions/ref/TXN-EA05201F-D04
```

---

#### Get Transactions by Sender

```http
GET /api/transactions/sender/{senderId}
```

---

#### Get Transactions by Receiver

```http
GET /api/transactions/receiver/{receiverId}
```

---

#### Update Transaction Status

```http
PATCH /api/transactions/{id}/status?status={STATUS}
```

**Example — mark as COMPLETED:**

```http
PATCH /api/transactions/1/status?status=COMPLETED
```

**Response — 200 OK**

```json
{
  "id": 1,
  "transactionRef": "TXN-EA05201F-D04",
  "senderId": 1,
  "receiverId": 2,
  "amount": 1500.0000,
  "currency": "GBP",
  "status": "COMPLETED",
  "description": "Invoice payment Q1",
  "createdAt": "2025-01-15T09:30:00",
  "updatedAt": "2025-01-15T10:45:00"
}
```

---

#### Delete Transaction

```http
DELETE /api/transactions/{id}
```

**Response — 204 No Content**

---

### Health & Monitoring — Actuator Endpoints

| Endpoint                                    | Description          |
|---------------------------------------------|----------------------|
| `GET /actuator/health`                      | Service health       |
| `GET /actuator/info`                        | Application info     |
| `GET /actuator/metrics`                     | Metrics list         |
| `GET /actuator/metrics/{metric.name}`       | Specific metric      |
| `GET http://localhost:8761`                 | Eureka dashboard     |

---

## Testing

### Quick smoke test — full platform

Run these in order after all services are started:

```bash
# 1. Create a user
curl -s -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jsmith",
    "email": "j.smith@jpmc.com",
    "firstName": "John",
    "lastName": "Smith"
  }' | jq .

# 2. Create a second user
curl -s -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "adavis",
    "email": "a.davis@jpmc.com",
    "firstName": "Alice",
    "lastName": "Davis"
  }' | jq .

# 3. Create a transaction
curl -s -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "senderId": 1,
    "receiverId": 2,
    "amount": 1500.00,
    "currency": "GBP",
    "description": "Invoice payment Q1"
  }' | jq .

# 4. Get all transactions
curl -s http://localhost:8080/api/transactions | jq .

# 5. Update transaction status
curl -s -X PATCH \
  "http://localhost:8080/api/transactions/1/status?status=COMPLETED" | jq .

# 6. Verify updated status
curl -s http://localhost:8080/api/transactions/1 | jq .

# 7. Get transactions by sender
curl -s http://localhost:8080/api/transactions/sender/1 | jq .
```

> **Note:** `jq` is optional. Remove `| jq .` if not installed.

---

## Troubleshooting

### Service fails to start — "Connection refused" to Config Server

**Cause:** Services started before Config Server was ready.

```bash
# Solution — verify Config Server is healthy first
curl http://localhost:8888/actuator/health
# Must return {"status":"UP"} before starting other services
```

---

### Service not appearing in Eureka dashboard

**Cause:** Eureka not running, or service started before Eureka was ready.

```bash
# Check Eureka is up
curl http://localhost:8761/actuator/health

# Check service eureka config
# application.yml must contain:
# eureka.client.service-url.defaultZone: http://localhost:8761/eureka/
```

---

### 503 Service Unavailable from API Gateway

**Cause:** Target service not yet registered with Eureka (allow 30–60 seconds after startup).

```bash
# Wait and retry — Eureka heartbeat interval is 30s by default
# Or check Eureka dashboard at http://localhost:8761
# The service name must appear under "Instances currently registered"
```

---

### PostgreSQL connection refused

**Cause:** Database not running, wrong port, or wrong credentials.

```bash
# Check PostgreSQL is running
pg_isready -h localhost -p 5432     # User DB
pg_isready -h localhost -p 5433     # Transaction DB

# Test connection manually
psql -h localhost -p 5432 -U userservice -d userdb
psql -h localhost -p 5433 -U transactionservice -d transactiondb

# Check PostgreSQL logs
tail -f /usr/local/var/log/postgresql@15.log   # macOS Homebrew
```

---

### 500 Internal Server Error on transaction creation

**Cause:** Database schema not created, or JPA/Hibernate dialect mismatch.

```bash
# Verify table exists
psql -U transactionservice -d transactiondb -c "\dt"

# Verify schema matches entity
psql -U transactionservice -d transactiondb -c "\d transactions"

# Enable SQL logging temporarily in application.yml
# spring.jpa.show-sql: true
# spring.jpa.properties.hibernate.format_sql: true
```

---

### Port already in use

```bash
# Find and kill process on a port
lsof -ti:8080 | xargs kill -9   # API Gateway
lsof -ti:8081 | xargs kill -9   # User Service
lsof -ti:8082 | xargs kill -9   # Transaction Service
lsof -ti:8761 | xargs kill -9   # Eureka
lsof -ti:8888 | xargs kill -9   # Config Server
```

---

## Roadmap

### Phase 2 — Resilience & Security

- [ ] **Inter-service communication** — Validate sender/receiver IDs via User Service before persisting transactions (OpenFeign client)
- [ ] **Global exception handling** — `@RestControllerAdvice` with standardised error response DTOs
- [ ] **Spring Security + JWT** — Token-based authentication enforced at the API Gateway
- [ ] **Input validation** — `@Valid` + Bean Validation annotations on all request DTOs
- [ ] **Circuit breaker** — Resilience4j for fault tolerance on inter-service calls

### Phase 3 — Event Streaming

- [ ] **Apache Kafka** — Publish `TransactionCreatedEvent` and `TransactionStatusChangedEvent` to Kafka topics
- [ ] **Notification Service** — Consume Kafka events and send email/SMS alerts
- [ ] **Audit Service** — Consume all events and write immutable audit log to a dedicated store

### Phase 4 — Observability

- [ ] **Distributed tracing** — Micrometer Tracing + Zipkin/Jaeger for end-to-end request tracing
- [ ] **Centralised logging** — Structured JSON logs shipped to ELK stack (Elasticsearch, Logstash, Kibana)
- [ ] **Metrics dashboard** — Prometheus + Grafana for service health and performance dashboards

### Phase 5 — New Services

- [ ] **Account Service** — Manage account balances; enforce balance checks before transaction approval
- [ ] **Payment Service** — Handle external payment gateway integrations
- [ ] **Fraud Detection Service** — Real-time transaction risk scoring

### Phase 6 — Infrastructure

- [ ] **Docker Compose** — Single-command local environment spin-up
- [ ] **Kubernetes (Helm charts)** — Production-grade container orchestration
- [ ] **CI/CD pipeline** — GitHub Actions with build, test, security scan, and deploy stages

---

## Contributing

1. Branch from `main` using the convention: `feature/`, `bugfix/`, `hotfix/`
2. Write tests for all new functionality
3. Ensure all services start cleanly before raising a PR
4. Never commit secrets, credentials, or environment-specific config files

---

## License

Internal use only — JP Microservices Platform.
```
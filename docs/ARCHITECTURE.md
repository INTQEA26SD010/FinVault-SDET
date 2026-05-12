# FinVault — System Architecture

> **FinVault** is a Smart-Card Budgeting System designed as a financial firewall.  
> It enforces disciplined spending by linking virtual smart cards to pre-defined budget envelopes,  
> giving users granular, real-time control over every transaction.

---

## Table of Contents

1. [Technology Stack](#technology-stack)
2. [High-Level System Design](#high-level-system-design)
3. [Communication Flow](#communication-flow)
4. [Directory Structure](#directory-structure)
5. [Key Architectural Decisions](#key-architectural-decisions)

---

## Technology Stack

### Backend

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 21 (LTS) |
| Framework | Spring Boot | 4.0.x |
| Persistence | Spring Data JPA + Hibernate ORM | 7.x |
| Database | MySQL | 8.0 |
| Security | Spring Security + JWT | — |
| Build Tool | Maven (via Maven Wrapper) | 3.x |

### Frontend

| Layer | Technology | Version |
|---|---|---|
| Framework | Angular | 21.x |
| Language | TypeScript | 5.x |
| UI / Design System | Bootstrap | 5.x |
| Package Manager | npm | 11.x |

### DevOps & Project Management

- **Version Control:** Git + GitHub (private enterprise repository: `FinVault-SDET`)
- **CI/CD:** GitHub Actions
- **Containerization:** Docker
- **Project Management:** Jira (Scrum) with Smart Commits
- **IDE:** VS Code with Atlassian + GitHub Copilot extensions

---

## High-Level System Design

FinVault follows a **decoupled, client-server architecture**. The Angular frontend
and the Spring Boot backend are completely independent applications that communicate
exclusively over a versioned REST API. This separation of concerns means either tier
can be developed, tested, scaled, or deployed independently.

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT TIER                             │
│                                                                 │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │          Angular 21  (localhost:4200)                   │   │
│   │                                                         │   │
│   │   ┌──────────┐   ┌──────────┐   ┌──────────────────┐   │   │
│   │   │Components│   │ Services │   │  Angular Router  │   │   │
│   │   └──────────┘   └────┬─────┘   └──────────────────┘   │   │
│   └────────────────────── │ ──────────────────────────────┘   │
│                            │  HTTP/JSON (REST)                 │
│                            │  Authorization: Bearer <JWT>      │
└────────────────────────────│────────────────────────────────────┘
                             │
┌────────────────────────────│────────────────────────────────────┐
│                         SERVER TIER                             │
│                                                                 │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │         Spring Boot 4  (localhost:8080)                 │   │
│   │                                                         │   │
│   │   ┌──────────────┐  ┌───────────┐  ┌────────────────┐  │   │
│   │   │  Controllers │→ │ Services  │→ │  Repositories  │  │   │
│   │   │  (REST API)  │  │(Business) │  │  (Spring Data) │  │   │
│   │   └──────────────┘  └───────────┘  └───────┬────────┘  │   │
│   │                                             │ JPA/SQL   │   │
│   │   ┌──────────────────────────────────────── │ ───────┐  │   │
│   │   │        Spring Security (JWT Filter)     │        │  │   │
│   │   └─────────────────────────────────────────│────────┘  │   │
│   └─────────────────────────────────────────────│───────────┘   │
│                                                 │               │
│   ┌─────────────────────────────────────────────│───────────┐   │
│   │             MySQL 8.0  (port 3306)          │           │   │
│   │             Database: finvault_db           ↓           │   │
│   │   ┌──────────┐  ┌──────────┐  ┌──────────────────────┐ │   │
│   │   │  users   │  │  cards   │  │     transactions     │ │   │
│   │   └──────────┘  └──────────┘  └──────────────────────┘ │   │
│   └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Communication Flow

1. The user interacts with the **Angular SPA** (Single Page Application) running at `localhost:4200`.
2. Angular's **HttpClient** sends an HTTP request (GET / POST / PUT / DELETE) to the Spring Boot REST API at `localhost:8080/api/v1/**`.
3. The **Spring Security JWT filter** intercepts every request, validates the Bearer token, and either grants access or returns `401 Unauthorized`.
4. The **`@RestController`** layer receives the validated request and delegates to a **`@Service`**.
5. The service executes business logic and calls a **Spring Data JPA `Repository`** interface.
6. Hibernate translates the repository call into **SQL** and executes it against **MySQL 8.0** (`finvault_db`).
7. The response travels back up the chain and is serialized to **JSON** by Jackson and returned to Angular.
8. Angular's service passes the response data to the relevant **component**, which re-renders the view reactively.

---

## Directory Structure

```
FinVault/                                     ← Monorepo root
│
├── backend/                                  ← Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/finvault/backend/
│   │   │   │   ├── BackendApplication.java   ← Main entry point (@SpringBootApplication)
│   │   │   │   ├── controller/               ← REST controllers (to be added)
│   │   │   │   ├── service/                  ← Business logic layer (to be added)
│   │   │   │   ├── repository/               ← Spring Data JPA interfaces (to be added)
│   │   │   │   ├── model/                    ← JPA @Entity classes (to be added)
│   │   │   │   └── security/                 ← JWT filter, config (to be added)
│   │   │   └── resources/
│   │   │       └── application.properties    ← MySQL, JPA, server config
│   │   └── test/
│   │       └── java/com/finvault/backend/
│   │           └── BackendApplicationTests.java
│   ├── pom.xml                               ← Maven dependencies & build config
│   └── mvnw / mvnw.cmd                       ← Maven wrapper scripts
│
├── frontend/                                 ← Angular application
│   ├── src/
│   │   ├── app/
│   │   │   ├── app.ts                        ← Root component
│   │   │   ├── app.html                      ← Root template
│   │   │   ├── app.routes.ts                 ← Application routing
│   │   │   └── app.config.ts                 ← App-level providers
│   │   ├── styles.css                        ← Global styles (post-Bootstrap overrides)
│   │   ├── index.html                        ← SPA shell
│   │   └── main.ts                           ← Angular bootstrap entry point
│   ├── angular.json                          ← CLI config (Bootstrap registered here)
│   ├── package.json                          ← npm dependencies
│   └── tsconfig.json                         ← TypeScript compiler config
│
├── docs/
│   └── Architecture.md                       ← This file
│
├── .github/                                  ← GitHub Actions CI/CD workflows (to be added)
├── .gitignore
└── README.md
```

---

## Key Architectural Decisions

| Decision | Choice | Rationale |
|---|---|---|
| **Decoupled architecture** | Separate Angular + Spring Boot apps | Independent deployability; frontend can be hosted on a CDN separately from the API |
| **JWT for auth** | Stateless tokens | Scalable, no server-side session storage; pairs well with future horizontal scaling |
| **Spring Data JPA** | Repository pattern | Eliminates boilerplate SQL; schema evolves with `ddl-auto=update` during development |
| **Bootstrap 5** | Registered globally in `angular.json` | Instant access to grid system and utilities across all components without per-file imports |
| **Maven Wrapper** | `mvnw` / `mvnw.cmd` checked in | Zero-dependency build; any developer can run `./mvnw spring-boot:run` without installing Maven |
| **Monorepo** | `backend/` + `frontend/` under one repo | Simplified Jira Smart Commit traceability; single PR covers full-stack changes |

---

*Last updated: Sprint 1 — SCRUM-9 (Backend Init) + SCRUM-10 (Frontend Init)*

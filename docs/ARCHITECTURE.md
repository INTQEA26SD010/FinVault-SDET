<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Angular-21-DD0031?style=for-the-badge&logo=angular&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
</p>

# 🏗️ FinVault — System Architecture

> **FinVault** is a **Smart-Card Budgeting System** designed as a *financial firewall*.  
> It enforces disciplined spending by linking virtual smart cards to pre-defined budget envelopes,  
> giving users **granular, real-time control** over every transaction.

---

## 📑 Table of Contents

| # | Section | Description |
|:-:|---------|-------------|
| 1 | [What is Software Architecture?](#-what-is-software-architecture) | Theory behind architectural design |
| 2 | [Technology Stack](#-technology-stack) | Languages, frameworks, and tools used |
| 3 | [Architecture Pattern — Client-Server](#-architecture-pattern--client-server) | Why FinVault uses a decoupled design |
| 4 | [High-Level System Design](#-high-level-system-design) | Visual diagram of the full system |
| 5 | [The 3-Layer Backend Pattern](#-the-3-layer-backend-pattern) | Controller → Service → Repository explained |
| 6 | [Request-Response Lifecycle](#-request-response-lifecycle) | Step-by-step flow of every HTTP request |
| 7 | [Directory Structure](#-directory-structure) | Complete file tree of the monorepo |
| 8 | [Key Architectural Decisions (ADRs)](#-key-architectural-decisions-adrs) | Design choices and their rationale |
| 9 | [Glossary](#-glossary) | Key terms and definitions |

---

## 📖 What is Software Architecture?

Before diving into FinVault's specifics, let's understand the **foundational concepts**:

### 🔹 Software Architecture — The Big Picture

Software architecture is the **high-level structure** of a software system. It defines:

- **How components are organized** (modules, layers, tiers)
- **How they communicate** (REST APIs, message queues, events)
- **What design constraints** they follow (security, scalability, maintainability)

> 💡 **Think of it like a building's blueprint** — it shows the rooms (components), doors (interfaces), plumbing (data flow), and wiring (communication protocols) before any construction begins.

### 🔹 Common Architecture Patterns

| Pattern | Description | Example |
|---------|-------------|---------|
| **Monolithic** | All code in a single deployable unit | Small internal tools |
| **Client-Server** ⭐ | Frontend (client) and backend (server) are separate | **FinVault** |
| **Microservices** | Backend split into many independent services | Netflix, Amazon |
| **Event-Driven** | Components communicate via events/messages | Real-time systems |
| **Serverless** | No managed servers; functions run on demand | AWS Lambda apps |

**FinVault uses the Client-Server pattern** — the most common pattern for modern web applications and the industry standard for full-stack development.

---

## 🛠️ Technology Stack

### Backend Technologies

| Layer | Technology | Version | Purpose |
|:-----:|:-----------|:-------:|---------|
| 🔤 Language | **Java** | 21 (LTS) | Industry-standard, strongly typed, enterprise-grade |
| 🚀 Framework | **Spring Boot** | 4.0.x | Auto-configuration, embedded server, dependency injection |
| 💾 ORM | **Spring Data JPA + Hibernate** | 7.x | Object-Relational Mapping — Java objects ↔ SQL tables |
| 🗄️ Database | **MySQL** | 8.0 | Open-source RDBMS with ACID transactions |
| 🔒 Security | **Spring Security + JWT** | — | Authentication, authorization, password hashing |
| 📦 Build Tool | **Maven** (via Maven Wrapper) | 3.x | Dependency management, build lifecycle |

### Frontend Technologies

| Layer | Technology | Version | Purpose |
|:-----:|:-----------|:-------:|---------|
| 🅰️ Framework | **Angular** | 21.x | Component-based SPA framework by Google |
| 📝 Language | **TypeScript** | 5.x | Typed superset of JavaScript |
| 🎨 UI Library | **Bootstrap** | 5.x | Responsive CSS framework with pre-built components |
| 📦 Package Manager | **npm** | 11.x | Node.js package manager |

### DevOps & Project Management

| Tool | Purpose |
|------|---------|
| 🔀 **Git + GitHub** | Version control (private enterprise repo: `FinVault-SDET`) |
| ⚡ **GitHub Actions** | CI/CD automation — auto-builds on every push/PR |
| 🐳 **Docker** | Containerization for consistent deployments |
| 📋 **Jira (Scrum)** | Sprint planning, backlog management, Smart Commits |
| 💻 **VS Code** | IDE with Atlassian + GitHub Copilot extensions |

---

## 🌐 Architecture Pattern — Client-Server

### What is Client-Server Architecture?

In a **client-server model**, the system is split into two distinct halves:

```
┌──────────────┐         HTTP / REST          ┌──────────────┐
│              │  ◄──────────────────────────► │              │
│   CLIENT     │     JSON request/response     │   SERVER     │
│  (Angular)   │                               │ (Spring Boot)│
│              │  Runs in user's browser        │ Runs on the  │
│  Port: 4200  │                               │ server machine│
│              │                               │  Port: 8080  │
└──────────────┘                               └──────────────┘
```

| Aspect | Client (Angular) | Server (Spring Boot) |
|--------|:----------------:|:--------------------:|
| **Runs on** | User's browser | Backend server (or localhost) |
| **Responsibility** | UI rendering, form validation, routing | Business logic, data persistence, security |
| **Language** | TypeScript | Java |
| **Communicates via** | HTTP requests (GET, POST, etc.) | JSON responses |
| **Can be deployed** | CDN, Nginx, Vercel | AWS EC2, Docker, Kubernetes |

### Why This Pattern for FinVault?

> ✅ **Independent Development** — Frontend and backend teams can work in parallel  
> ✅ **Independent Deployment** — Deploy the Angular app on a CDN, the API on a server  
> ✅ **Independent Scaling** — Scale the backend horizontally without touching the frontend  
> ✅ **Technology Freedom** — Either side can be rewritten without affecting the other  

---

## 🗺️ High-Level System Design

The diagram below shows every major component in FinVault and how they connect:

```
╔═══════════════════════════════════════════════════════════════════╗
║                        🌐 CLIENT TIER                            ║
║                                                                   ║
║   ┌───────────────────────────────────────────────────────────┐   ║
║   │              Angular 21  (localhost:4200)                 │   ║
║   │                                                           │   ║
║   │   ┌─────────────┐  ┌────────────┐  ┌──────────────────┐  │   ║
║   │   │ Components  │  │  Services  │  │  Angular Router  │  │   ║
║   │   │ (Login,     │  │ (AuthSvc,  │  │  (app.routes.ts) │  │   ║
║   │   │  Dashboard) │  │  HttpClient│  │                  │  │   ║
║   │   └─────────────┘  └─────┬──────┘  └──────────────────┘  │   ║
║   └───────────────────────── │ ───────────────────────────────┘   ║
║                               │                                    ║
║                               │  HTTP / JSON (REST API)            ║
║                               │  Authorization: Bearer <JWT>       ║
╠═══════════════════════════════╪════════════════════════════════════╣
║                        🖥️ SERVER TIER                             ║
║                               │                                    ║
║   ┌───────────────────────────│────────────────────────────────┐  ║
║   │          Spring Boot 4  (localhost:8080)                   │  ║
║   │                           │                                │  ║
║   │   ┌───────────────┐  ┌───▼────────┐  ┌─────────────────┐  │  ║
║   │   │  🎯 Controllers│→ │ ⚙️ Services │→ │ 💾 Repositories │  │  ║
║   │   │  (REST API)    │  │ (Business  │  │ (Spring Data    │  │  ║
║   │   │  AuthController│  │  Logic)    │  │  JPA)           │  │  ║
║   │   │  CardController│  │ UserSvc    │  │ UserRepo        │  │  ║
║   │   └───────────────┘  │ CardSvc    │  │ CardRepo        │  │  ║
║   │                       └────────────┘  └────────┬────────┘  │  ║
║   │                                                │ JPA/SQL   │  ║
║   │   ┌────────────────────────────────────────────│────────┐  │  ║
║   │   │     🔒 Spring Security (JWT Filter Chain)  │        │  │  ║
║   │   │     BCryptPasswordEncoder                  │        │  │  ║
║   │   └────────────────────────────────────────────│────────┘  │  ║
║   └────────────────────────────────────────────────│───────────┘  ║
║                                                    │              ║
║   ┌────────────────────────────────────────────────│───────────┐  ║
║   │              🗄️ MySQL 8.0  (port 3306)         │           │  ║
║   │              Database: finvault_db             ▼           │  ║
║   │   ┌──────────────┐  ┌──────────────────┐  ┌───────────┐  │  ║
║   │   │    users     │  │  virtual_cards   │  │ (future   │  │  ║
║   │   │              │  │                  │  │  tables)  │  │  ║
║   │   └──────┬───────┘  └────────┬─────────┘  └───────────┘  │  ║
║   │          │    1 : Many       │                             │  ║
║   │          └───────────────────┘                             │  ║
║   └────────────────────────────────────────────────────────────┘  ║
╚═══════════════════════════════════════════════════════════════════╝
```

---

## 🧱 The 3-Layer Backend Pattern

FinVault's backend follows a strict **3-Layer Architecture** — a well-established design pattern in enterprise Java development:

### What are the 3 Layers?

```
        ┌──────────────────────────────────────────────┐
        │              📨 CONTROLLER LAYER              │
        │     Receives HTTP requests, returns responses │
        │     AuthController, VirtualCardController     │
        └───────────────────┬──────────────────────────┘
                            │  calls ↓
        ┌───────────────────▼──────────────────────────┐
        │              ⚙️ SERVICE LAYER                 │
        │     Contains ALL business logic               │
        │     UserService, VirtualCardService           │
        └───────────────────┬──────────────────────────┘
                            │  calls ↓
        ┌───────────────────▼──────────────────────────┐
        │              💾 REPOSITORY LAYER              │
        │     Talks to the database via JPA             │
        │     UserRepository, VirtualCardRepository     │
        └──────────────────────────────────────────────┘
```

### Why 3 Layers?

| Principle | Without Layers | With Layers |
|-----------|:--------------:|:-----------:|
| **Separation of Concerns** | Controller does everything — messy! | Each layer has ONE job |
| **Testability** | Must boot full server to test logic | Mock a layer, test in isolation |
| **Maintainability** | Change one thing, break everything | Change one layer, others unaffected |
| **Reusability** | Can't reuse logic across endpoints | Services can be called by multiple controllers |

### Layer Rules (Golden Rules)

> 🚫 **Controllers** must NEVER access the database directly  
> 🚫 **Services** must NEVER import `HttpServletRequest` or return `ResponseEntity`  
> 🚫 **Repositories** must NEVER contain business logic  
> ✅ Data flows **one way**: Controller → Service → Repository  

---

## 🔄 Request-Response Lifecycle

Here is the **step-by-step journey** of every HTTP request through FinVault:

```
 👤 User                   🌐 Angular               🖥️ Spring Boot           🗄️ MySQL
  │                          │                          │                       │
  │  1. Fills form           │                          │                       │
  │  & clicks submit         │                          │                       │
  │─────────────────────────►│                          │                       │
  │                          │  2. AuthService sends    │                       │
  │                          │  POST /api/auth/register │                       │
  │                          │─────────────────────────►│                       │
  │                          │                          │  3. SecurityFilter     │
  │                          │                          │  checks JWT token     │
  │                          │                          │  (permitAll for now)  │
  │                          │                          │                       │
  │                          │                          │  4. AuthController    │
  │                          │                          │  receives request     │
  │                          │                          │  & calls UserService  │
  │                          │                          │                       │
  │                          │                          │  5. UserService       │
  │                          │                          │  hashes password      │
  │                          │                          │  with BCrypt          │
  │                          │                          │                       │
  │                          │                          │  6. UserRepository    │
  │                          │                          │  .save(user)          │
  │                          │                          │──────────────────────►│
  │                          │                          │                       │
  │                          │                          │  7. INSERT INTO users │
  │                          │                          │◄──────────────────────│
  │                          │                          │                       │
  │                          │  8. 201 Created          │                       │
  │                          │  { message, userId }     │                       │
  │                          │◄─────────────────────────│                       │
  │                          │                          │                       │
  │  9. Shows success alert  │                          │                       │
  │  & redirects to /dashboard                          │                       │
  │◄─────────────────────────│                          │                       │
  │                          │                          │                       │
```

### The 9 Steps Explained

| Step | Component | What Happens |
|:----:|-----------|--------------|
| **1** | 👤 Browser | User fills in username, email, password and clicks "Register" |
| **2** | 🅰️ Angular `AuthService` | `HttpClient.post()` sends JSON payload to `http://localhost:8080/api/auth/register` |
| **3** | 🔒 Spring Security Filter | Intercepts the request; currently `permitAll()` — JWT will be enforced later |
| **4** | 🎯 `AuthController` | `@PostMapping("/register")` method receives the `UserRegistrationDto` from `@RequestBody` |
| **5** | ⚙️ `UserService` | Checks email uniqueness, hashes password with `BCryptPasswordEncoder.encode()` |
| **6** | 💾 `UserRepository` | `save(user)` — Spring Data JPA translates to a Hibernate `persist()` call |
| **7** | 🗄️ MySQL | Hibernate generates: `INSERT INTO users (username, email, password_hash, created_at) VALUES (?, ?, ?, ?)` |
| **8** | 🎯 `AuthController` | Returns `ResponseEntity.status(201).body({ message, userId })` — serialized to JSON by Jackson |
| **9** | 🅰️ Angular `LoginComponent` | Shows success alert, then `router.navigate(['/dashboard'])` after 1.5s delay |

---

## 📂 Directory Structure

```
FinVault/                                          ← 📁 Monorepo root
│
├── 📂 backend/                                    ← Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/finvault/backend/
│   │   │   │   ├── BackendApplication.java        ← 🚀 Main entry point (@SpringBootApplication)
│   │   │   │   ├── config/
│   │   │   │   │   └── SecurityConfig.java        ← 🔒 BCrypt bean + Security filter chain
│   │   │   │   ├── controller/
│   │   │   │   │   ├── AuthController.java        ← 🎯 POST /api/auth/register
│   │   │   │   │   └── VirtualCardController.java ← 🎯 GET  /api/cards/user/{userId}
│   │   │   │   ├── dto/
│   │   │   │   │   ├── UserRegistrationDto.java   ← 📨 Inbound: registration request body
│   │   │   │   │   └── VirtualCardResponseDto.java← 📤 Outbound: card data (CVV excluded)
│   │   │   │   ├── entity/
│   │   │   │   │   ├── User.java                  ← 💾 @Entity → users table
│   │   │   │   │   └── VirtualCard.java           ← 💾 @Entity → virtual_cards table
│   │   │   │   ├── repository/
│   │   │   │   │   ├── UserRepository.java        ← 🔍 JpaRepository<User, Long>
│   │   │   │   │   └── VirtualCardRepository.java ← 🔍 JpaRepository<VirtualCard, Long>
│   │   │   │   └── service/
│   │   │   │       ├── UserService.java           ← ⚙️ Registration + BCrypt hashing
│   │   │   │       └── VirtualCardService.java    ← ⚙️ Card fetching + DTO mapping
│   │   │   └── resources/
│   │   │       └── application.properties         ← ⚙️ MySQL, JPA, server config
│   │   └── test/
│   │       └── java/com/finvault/backend/
│   │           └── BackendApplicationTests.java   ← 🧪 Spring Boot context test
│   ├── pom.xml                                    ← 📦 Maven dependencies & build config
│   └── mvnw / mvnw.cmd                           ← 🔧 Maven wrapper scripts
│
├── 📂 frontend/                                   ← Angular application
│   ├── src/
│   │   ├── app/
│   │   │   ├── app.ts                             ← 🅰️ Root component (shell)
│   │   │   ├── app.html                           ← 🅰️ <router-outlet /> only
│   │   │   ├── app.routes.ts                      ← 🛤️ Application routing table
│   │   │   ├── app.config.ts                      ← ⚙️ App providers (Router, HttpClient)
│   │   │   ├── services/
│   │   │   │   └── auth.service.ts                ← 📡 HTTP calls to /api/auth
│   │   │   ├── login/
│   │   │   │   ├── login.component.ts             ← 📝 Registration form logic
│   │   │   │   └── login.component.html           ← 🎨 Bootstrap login card UI
│   │   │   └── dashboard/
│   │   │       ├── dashboard.component.ts         ← 📊 Mock card data (→ API in SCRUM-16)
│   │   │       └── dashboard.component.html       ← 🎨 Sidebar + card grid layout
│   │   ├── styles.css                             ← 🎨 Global styles
│   │   ├── index.html                             ← 📄 SPA shell
│   │   └── main.ts                                ← 🚀 Angular bootstrap entry point
│   ├── angular.json                               ← ⚙️ CLI config (Bootstrap registered here)
│   ├── package.json                               ← 📦 npm dependencies
│   └── tsconfig.json                              ← 📝 TypeScript compiler config
│
├── 📂 docs/                                       ← Documentation
│   ├── ARCHITECTURE.md                            ← 🏗️ This file
│   ├── API_DOCS.md                                ← 📡 REST API documentation
│   ├── DB_SCHEMA.md                               ← 🗄️ Database schema design
│   ├── JPA_ENTITIES.md                            ← 💾 Entity & Repository docs
│   ├── FRONTEND_DOCS.md                           ← 🅰️ Angular frontend docs
│   └── CI_PIPELINE.md                             ← ⚡ GitHub Actions pipeline docs
│
├── .github/                                       ← ⚡ GitHub Actions CI/CD workflows
├── .gitignore
└── README.md                                      ← 📖 Project overview
```

---

## 🏛️ Key Architectural Decisions (ADRs)

> **ADR** = Architecture Decision Record — a lightweight method for documenting important design choices.

| # | Decision | Choice Made | Rationale |
|:-:|----------|-------------|-----------|
| 1 | **Decoupled architecture** | Separate Angular + Spring Boot apps | Independent deployability; frontend can be hosted on a CDN separately from the API server |
| 2 | **Authentication method** | JWT (JSON Web Tokens) | Stateless, scalable — no server-side session storage needed; pairs well with horizontal scaling |
| 3 | **ORM vs Raw SQL** | Spring Data JPA (Repository pattern) | Eliminates boilerplate SQL; schema evolves with `ddl-auto=update` during development |
| 4 | **CSS framework** | Bootstrap 5 (global via `angular.json`) | Instant access to grid system and utilities across all components without per-file imports |
| 5 | **Build tool** | Maven Wrapper (`mvnw` / `mvnw.cmd`) | Zero-dependency build — any developer can run `./mvnw spring-boot:run` without installing Maven |
| 6 | **Repository strategy** | Monorepo (`backend/` + `frontend/`) | Simplified Jira Smart Commit traceability; single PR covers full-stack changes |
| 7 | **Password storage** | BCrypt (strength 10) | Industry-standard adaptive hashing — plaintext passwords are NEVER stored |
| 8 | **API data format** | DTOs (Data Transfer Objects) | Prevents exposing JPA entity internals (passwords, CVV) in API responses |

---

## 📚 Glossary

| Term | Definition |
|------|-----------|
| **SPA** | Single Page Application — the browser loads one HTML page, and Angular handles navigation dynamically via JavaScript |
| **REST** | Representational State Transfer — an architectural style for building web APIs using standard HTTP methods |
| **JWT** | JSON Web Token — a compact, self-contained token used for stateless authentication between client and server |
| **JPA** | Java Persistence API — a specification for mapping Java objects to relational database tables |
| **ORM** | Object-Relational Mapping — the technique of converting data between a relational DB and object-oriented code |
| **DTO** | Data Transfer Object — a plain Java class used to transfer data between layers without exposing entity internals |
| **BCrypt** | A password hashing algorithm that incorporates a salt and a configurable work factor to resist brute-force attacks |
| **CSRF** | Cross-Site Request Forgery — an attack where a malicious site tricks a user's browser into making unwanted requests |
| **ADR** | Architecture Decision Record — a concise document capturing a significant architectural decision and its rationale |
| **Monorepo** | A single Git repository that contains multiple distinct projects (e.g., frontend and backend) |

---

<p align="center">
  <b>📐 FinVault Architecture Document</b><br>
  <sub>Sprint 1 — SCRUM-9 (Backend Init) + SCRUM-10 (Frontend Init)</sub><br>
  <sub>Part of the <a href="API_DOCS.md">FinVault Documentation Suite</a></sub>
</p>

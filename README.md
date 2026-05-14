<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Angular-21-DD0031?style=for-the-badge&logo=angular&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/TypeScript-5.x-3178C6?style=for-the-badge&logo=typescript&logoColor=white" />
  <img src="https://img.shields.io/badge/Bootstrap-5-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white" />
</p>

<h1 align="center">💳 FinVault</h1>

<p align="center">
  <b>Smart-Card Budgeting System — Your Financial Firewall</b><br>
  <sub>SDET Final Training Project | Built with Spring Boot 4 + Angular 21</sub>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/status-Sprint%201%20Complete-brightgreen?style=flat-square" />
  <img src="https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white" />
  <img src="https://img.shields.io/badge/license-Private-red?style=flat-square" />
</p>

---

## 🧐 What is FinVault?

**FinVault** is a full-stack **Smart-Card Budgeting System** designed as a *financial firewall*. It enforces disciplined spending by linking **virtual smart cards** to pre-defined budget envelopes, giving users **granular, real-time control** over every transaction.

> Think of each virtual card as a **spending boundary** — you set a daily limit, assign it to a category (groceries, fuel, entertainment), and the card blocks any transaction that exceeds your budget. Freeze it instantly. Unfreeze when ready. Full control, zero overspending.

### Key Features (Sprint 1)

| Feature | Status | Description |
|---------|:------:|-------------|
| 🔐 User Registration | ✅ Done | Secure signup with BCrypt password hashing |
| 💳 Virtual Card Dashboard | ✅ Done | View, manage, and monitor all virtual cards |
| 📡 REST API | ✅ Done | Register users + fetch cards via JSON endpoints |
| 🗄️ MySQL Schema | ✅ Done | Normalized 3NF schema with FK constraints |
| ⚡ CI Pipeline | ✅ Done | Auto-build on every push/PR via GitHub Actions |
| 🔒 JWT Authentication | 🔜 Next | Stateless token-based security |
| 📊 Transaction Tracking | 🔜 Planned | Real-time spending history and analytics |
| 🧪 SDET Testing Suite | 🔜 Planned | JUnit + Mockito + Selenium + RestAssured |

---

## 🏗️ Architecture Overview

FinVault follows a **decoupled client-server architecture** — the Angular frontend and Spring Boot backend are completely independent applications communicating over REST/JSON.

```
╔══════════════════════════════════════════════════════════════════╗
║   🌐 FRONTEND (Angular 21)          🖥️ BACKEND (Spring Boot 4)  ║
║   ─────────────────────              ─────────────────────────   ║
║   • Login Page                       • AuthController            ║
║   • Dashboard                 HTTP   • VirtualCardController     ║
║   • AuthService          ◄────────►  • UserService               ║
║   • Bootstrap 5 UI         JSON      • VirtualCardService        ║
║   localhost:4200                     localhost:8080               ║
║                                             │                    ║
║                                             ▼                    ║
║                                      🗄️ MySQL 8.0               ║
║                                      finvault_db                ║
║                                      • users table               ║
║                                      • virtual_cards table       ║
╚══════════════════════════════════════════════════════════════════╝
```

---

## 🛠️ Tech Stack

### Backend

| Technology | Version | Purpose |
|:----------:|:-------:|---------|
| Java | 21 LTS | Core language |
| Spring Boot | 4.0.x | Web framework + auto-configuration |
| Spring Data JPA | — | ORM + Repository pattern |
| Hibernate | 7.x | JPA implementation — SQL generation |
| Spring Security | — | BCrypt password hashing (JWT in next sprint) |
| MySQL | 8.0 | Relational database |
| Maven | 3.x (Wrapper) | Build tool + dependency management |
| Lombok | — | Boilerplate elimination |

### Frontend

| Technology | Version | Purpose |
|:----------:|:-------:|---------|
| Angular | 21.x | Component-based SPA framework |
| TypeScript | 5.x | Typed superset of JavaScript |
| Bootstrap | 5.x | Responsive CSS framework |
| npm | 11.x | Package manager |

### DevOps

| Tool | Purpose |
|:----:|---------|
| Git + GitHub | Version control (private enterprise repo) |
| GitHub Actions | CI/CD — auto-build on push/PR |
| Docker | Containerization (planned) |
| Jira (Scrum) | Sprint planning + Smart Commits |

---

## 📂 Project Structure

```
FinVault/
│
├── 📂 backend/                          ← Spring Boot Application
│   ├── src/main/java/com/finvault/backend/
│   │   ├── BackendApplication.java      ← 🚀 Entry point
│   │   ├── config/
│   │   │   └── SecurityConfig.java      ← 🔒 BCrypt + Security filter
│   │   ├── controller/
│   │   │   ├── AuthController.java      ← POST /api/auth/register
│   │   │   └── VirtualCardController.java ← GET /api/cards/user/{id}
│   │   ├── dto/
│   │   │   ├── UserRegistrationDto.java ← Inbound request body
│   │   │   └── VirtualCardResponseDto.java ← Outbound (CVV excluded)
│   │   ├── entity/
│   │   │   ├── User.java               ← @Entity → users table
│   │   │   └── VirtualCard.java         ← @Entity → virtual_cards table
│   │   ├── repository/
│   │   │   ├── UserRepository.java      ← JpaRepository<User, Long>
│   │   │   └── VirtualCardRepository.java
│   │   └── service/
│   │       ├── UserService.java         ← Registration + BCrypt
│   │       └── VirtualCardService.java  ← Card fetch + DTO mapping
│   ├── src/main/resources/
│   │   └── application.properties       ← MySQL + JPA config
│   └── pom.xml                          ← Maven dependencies
│
├── 📂 frontend/                         ← Angular Application
│   ├── src/app/
│   │   ├── app.ts / app.html            ← Root component (<router-outlet>)
│   │   ├── app.routes.ts               ← Route definitions
│   │   ├── app.config.ts               ← Providers (Router, HttpClient)
│   │   ├── services/
│   │   │   └── auth.service.ts          ← HTTP calls to backend
│   │   ├── login/                       ← Registration form
│   │   └── dashboard/                   ← Card management UI
│   ├── angular.json                     ← CLI config (Bootstrap here)
│   └── package.json                     ← npm dependencies
│
├── 📂 docs/                             ← 📖 Documentation Suite
│   ├── ARCHITECTURE.md                  ← System design & patterns
│   ├── API_DOCS.md                      ← REST API reference
│   ├── DB_SCHEMA.md                     ← Database schema & SQL
│   ├── JPA_ENTITIES.md                  ← Entity & repository docs
│   ├── FRONTEND_DOCS.md                 ← Angular frontend docs
│   └── CI_PIPELINE.md                   ← GitHub Actions pipeline
│
├── .github/workflows/                   ← CI/CD pipeline
└── README.md                            ← 📍 You are here
```

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version | Download |
|:----:|:-------:|:--------:|
| Java JDK | 21+ | [Eclipse Temurin](https://adoptium.net/) |
| MySQL | 8.0+ | [MySQL Community](https://dev.mysql.com/downloads/) |
| Node.js | 20+ | [Node.js](https://nodejs.org/) |
| npm | 11+ | Bundled with Node.js |

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/FinVault-SDET.git
cd FinVault-SDET
```

### 2. Set Up the Database

```sql
CREATE DATABASE IF NOT EXISTS finvault_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

> The tables (`users`, `virtual_cards`) are auto-created by Hibernate on first run via `ddl-auto=update`.

### 3. Start the Backend

```bash
cd backend
./mvnw spring-boot:run
```

> **Windows:** Use `mvnw.cmd spring-boot:run`  
> Backend starts at **http://localhost:8080**

### 4. Start the Frontend

```bash
cd frontend
npm install
ng serve
```

> Frontend starts at **http://localhost:4200**

### 5. Verify It Works

```bash
# Register a user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","email":"john@example.com","password":"SecurePass123"}'

# Expected: 201 Created → {"message":"User registered successfully","userId":1}
```

---

## 📡 API Quick Reference

| Method | Endpoint | Description | Status |
|:------:|----------|-------------|:------:|
| `POST` | `/api/auth/register` | Register a new user | ✅ |
| `GET` | `/api/cards/user/{userId}` | Get user's virtual cards | ✅ |
| `POST` | `/api/auth/login` | Authenticate (JWT) | 🔜 |
| `POST` | `/api/cards` | Create a virtual card | 🔜 |
| `PUT` | `/api/cards/{id}/freeze` | Freeze a card | 🔜 |
| `DELETE` | `/api/cards/{id}` | Cancel a card | 🔜 |

> 📖 Full API documentation: [docs/API_DOCS.md](docs/API_DOCS.md)

---

## 📖 Documentation Suite

All project documentation is in the `docs/` folder — designed as comprehensive study material:

| Document | Description | Link |
|----------|-------------|:----:|
| 🏗️ **Architecture** | System design, tech stack, 3-layer pattern, request lifecycle | [ARCHITECTURE.md](docs/ARCHITECTURE.md) |
| 📡 **REST API** | Endpoints, HTTP methods, status codes, DTOs, cURL examples | [API_DOCS.md](docs/API_DOCS.md) |
| 🗄️ **Database Schema** | ER diagram, SQL DDL, normalization, constraints, ACID theory | [DB_SCHEMA.md](docs/DB_SCHEMA.md) |
| 💾 **JPA Entities** | ORM theory, entity mapping, repositories, Lombok, pitfalls | [JPA_ENTITIES.md](docs/JPA_ENTITIES.md) |
| 🅰️ **Frontend** | Angular concepts, SPA theory, components, routing, forms | [FRONTEND_DOCS.md](docs/FRONTEND_DOCS.md) |
| ⚡ **CI/CD Pipeline** | GitHub Actions, Maven lifecycle, caching, branch protection | [CI_PIPELINE.md](docs/CI_PIPELINE.md) |

---

## ⚡ CI/CD Pipeline

Every push to `main` and every pull request triggers the **GitHub Actions** pipeline:

```
  Push / PR to main
       │
       ▼
  ┌────────────────────────────┐
  │ ☑ Checkout repository      │
  │ ☑ Setup Java 21 (Temurin)  │
  │ ☑ Restore Maven cache      │
  │ ☑ mvnw package -DskipTests │
  └────────────────┬───────────┘
                   │
      ┌────────────┴────────────┐
      ▼                         ▼
  ✅ Pass → Merge allowed    ❌ Fail → PR blocked
```

> 📖 Full pipeline docs: [docs/CI_PIPELINE.md](docs/CI_PIPELINE.md)

---

## 🗺️ Sprint Roadmap

| Sprint | Focus | Status |
|:------:|-------|:------:|
| **Sprint 1** | Project setup, DB schema, JPA entities, REST APIs, Angular UI, CI pipeline | ✅ Complete |
| **Sprint 2** | JWT authentication, login flow, route guards, card CRUD operations | 🔜 Next |
| **Sprint 3** | Transaction tracking, budget alerts, spending analytics | 📋 Planned |
| **Sprint 4** | SDET testing (JUnit, Mockito, Selenium, RestAssured) | 📋 Planned |
| **Sprint 5** | Docker containerization, deployment, performance optimization | 📋 Planned |

---

## 👥 Team & Project Management

| Aspect | Details |
|--------|---------|
| **Methodology** | Agile Scrum |
| **Sprint Duration** | 2 weeks |
| **Project Board** | Jira with Smart Commits |
| **Version Control** | GitHub (private enterprise repo) |
| **IDE** | VS Code with Copilot + Atlassian extensions |

### Jira Smart Commits

```bash
# Automatically transitions Jira tickets via commit messages:
git commit -m "SCRUM-14 #done Implemented REST API endpoints for registration and card fetching"
```

---

<p align="center">
  <b>💳 FinVault — Smart-Card Budgeting System</b><br>
  <sub>Built with Spring Boot 4 + Angular 21 + MySQL 8.0</sub><br>
  <sub>SDET Final Training Project © 2026</sub>
</p>

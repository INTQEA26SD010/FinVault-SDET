<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Angular-21.2-DD0031?style=for-the-badge&logo=angular&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/TypeScript-5.9-3178C6?style=for-the-badge&logo=typescript&logoColor=white" />
  <img src="https://img.shields.io/badge/Bootstrap-5.3-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white" />
</p>

<h1 align="center">💳 FinVault</h1>

<p align="center">
  <b>Smart-Card Budgeting System — Your Financial Firewall</b><br>
  <sub>SDET Final Training Project | Built with Spring Boot 4 + Angular 21</sub>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/status-Hardening%20Sprint%20Complete-brightgreen?style=flat-square" />
  <img src="https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white" />
  <img src="https://img.shields.io/badge/endpoints-8%20REST%20APIs-6DB33F?style=flat-square" />
  <img src="https://img.shields.io/badge/license-Private-red?style=flat-square" />
</p>

---

## 🧐 What is FinVault?

**FinVault** is a full-stack **Smart-Card Budgeting System** designed as a *financial firewall*. It enforces disciplined spending by linking **virtual smart cards** to pre-defined budget envelopes, giving users **granular, real-time control** over every transaction.

> Think of each virtual card as a **spending boundary** — you set a daily limit, assign it to a vendor (Amazon, Netflix, groceries), and the card blocks any transaction that exceeds your budget. Freeze it instantly. Unfreeze when ready. Delete when done. Full control, zero overspending.

---

## ✅ Implemented Features

| Feature | Endpoint / Route | Description |
|---------|:----------------:|-------------|
| 🔐 User Registration | `POST /api/auth/register` | Secure signup with BCrypt password hashing |
| 🔑 User Login | `POST /api/auth/login` | Email + password authentication with session management |
| 💳 Virtual Card Creation | `POST /api/cards` | Generate cards with vendor name + custom daily limit |
| 📋 Card Dashboard | `GET /api/cards/user/{id}` | View, manage, and monitor all virtual cards |
| 🔒 Freeze / Unfreeze | `PUT /api/cards/{id}/toggle` | Toggle card status ACTIVE ↔ FROZEN in one click |
| 🗑️ Delete Card | `DELETE /api/cards/{id}` | Permanently remove card + cascading transaction cleanup |
| 💸 Transaction Simulator | `POST /api/transactions` | Simulate purchases with approve/decline logic |
| 📊 Transaction History | `GET /api/transactions/card/{id}` | Full audit trail per card, newest-first |
| 🛡️ Route Guards | `/dashboard`, `/simulator` | Angular `authGuard` blocks unauthenticated access |
| 🧪 Dedicated Simulator | `/simulator` route | QA-friendly transaction testing tool |
| ⚡ CI Pipeline | GitHub Actions | Auto-build on every push/PR to `main` |

---

## 🏗️ Architecture Overview

FinVault follows a **decoupled client-server architecture** — the Angular frontend and Spring Boot backend are completely independent applications communicating over REST/JSON.

```
╔══════════════════════════════════════════════════════════════════════════╗
║   🌐 FRONTEND (Angular 21)              🖥️ BACKEND (Spring Boot 4)      ║
║   ─────────────────────────              ──────────────────────────────  ║
║   • LoginComponent                       • AuthController               ║
║   • DashboardComponent            HTTP   • VirtualCardController        ║
║   • SimulatorComponent        ◄────────► • TransactionController        ║
║   • AuthService / VirtualCard   JSON     • UserService                  ║
║     Service                              • VirtualCardService           ║
║   • authGuard                            • TransactionService           ║
║   localhost:4200                         localhost:8080                  ║
║                                                 │                       ║
║                                                 ▼                       ║
║                                          🗄️ MySQL 8.0                   ║
║                                          finvault_db                    ║
║                                          • users table                  ║
║                                          • virtual_cards table          ║
║                                          • transactions table           ║
╚══════════════════════════════════════════════════════════════════════════╝
```

---

## 🛠️ Tech Stack

### Backend

| Technology | Version | Purpose |
|:----------:|:-------:|---------|
| Java | 21 LTS | Core language |
| Spring Boot | 4.0.6 | Web framework + auto-configuration |
| Spring Data JPA | — | ORM + Repository pattern |
| Hibernate | 7.x | JPA implementation — SQL generation |
| Spring Security | — | BCrypt password hashing + CORS configuration |
| MySQL | 8.0 | Relational database with ACID guarantees |
| Maven | 3.x (Wrapper) | Build tool + dependency management |
| Lombok | — | Boilerplate elimination (`@Data`, `@RequiredArgsConstructor`) |

### Frontend

| Technology | Version | Purpose |
|:----------:|:-------:|---------|
| Angular | 21.2 | Component-based SPA framework (standalone components) |
| TypeScript | 5.9 | Typed superset of JavaScript |
| Bootstrap | 5.3 | Responsive CSS framework |
| RxJS | 7.8 | Reactive programming for HTTP streams |
| npm | 11.x | Package manager |

### DevOps

| Tool | Purpose |
|:----:|---------|
| Git + GitHub | Version control (private enterprise repo) |
| GitHub Actions | CI/CD — auto-build on push/PR |
| Jira (Scrum) | Sprint planning + Smart Commits |
| Docker | Containerization (planned) |

---

## 📂 Project Structure

```
FinVault/
│
├── 📂 backend/                              ← Spring Boot Application
│   ├── src/main/java/com/finvault/backend/
│   │   ├── BackendApplication.java          ← 🚀 Entry point
│   │   ├── config/
│   │   │   └── SecurityConfig.java          ← 🔒 BCrypt bean + CORS + permitAll filter
│   │   ├── controller/
│   │   │   ├── AuthController.java          ← POST /register, POST /login
│   │   │   ├── VirtualCardController.java   ← GET cards, POST create, PUT toggle, DELETE
│   │   │   └── TransactionController.java   ← POST simulate, GET history
│   │   ├── dto/
│   │   │   ├── UserRegistrationDto.java     ← Inbound: { username, email, password }
│   │   │   ├── LoginRequestDto.java         ← Inbound: { email, password }
│   │   │   ├── LoginResponseDto.java        ← Outbound: { userId, username, email, message }
│   │   │   ├── CreateVirtualCardDto.java    ← Inbound: { userId, dailyLimit, vendorName }
│   │   │   ├── VirtualCardResponseDto.java  ← Outbound: card fields (including vendorName)
│   │   │   ├── TransactionRequestDto.java   ← Inbound: { cardId, amount, merchantName }
│   │   │   └── TransactionResponseDto.java  ← Outbound: { id, cardId, amount, merchantName, timestamp, status }
│   │   ├── entity/
│   │   │   ├── User.java                    ← @Entity → users table
│   │   │   ├── VirtualCard.java             ← @Entity → virtual_cards + CardStatus enum
│   │   │   └── Transaction.java             ← @Entity → transactions + TransactionStatus enum
│   │   ├── repository/
│   │   │   ├── UserRepository.java          ← JpaRepository<User, Long>
│   │   │   ├── VirtualCardRepository.java   ← findByUserId, findByCardNumber
│   │   │   └── TransactionRepository.java   ← findByVirtualCardIdOrderByTimestampDesc
│   │   └── service/
│   │       ├── UserService.java             ← Registration + BCrypt + Login validation
│   │       ├── VirtualCardService.java      ← CRUD + toggle + DTO mapping
│   │       └── TransactionService.java      ← Approve/decline engine + audit trail
│   ├── src/main/resources/
│   │   └── application.properties           ← MySQL + JPA + logging config
│   └── pom.xml                              ← Maven dependencies
│
├── 📂 frontend/                             ← Angular 21 Application
│   ├── src/app/
│   │   ├── app.ts / app.html                ← Root component (<router-outlet />)
│   │   ├── app.routes.ts                    ← Route definitions (login, dashboard, simulator)
│   │   ├── app.config.ts                    ← Providers (Router, HttpClient with Fetch)
│   │   ├── guards/
│   │   │   └── auth.guard.ts                ← 🔒 CanActivateFn — blocks unauthenticated access
│   │   ├── services/
│   │   │   ├── auth.service.ts              ← Login/register HTTP + sessionStorage session
│   │   │   └── virtual-card.service.ts      ← Cards + transactions HTTP calls
│   │   ├── login/                           ← Login + Signup tabbed form
│   │   ├── dashboard/                       ← Card dashboard with 3-tab sidebar
│   │   └── simulator/                       ← QA transaction testing tool
│   ├── angular.json                         ← CLI config (Bootstrap configured here)
│   └── package.json                         ← npm dependencies
│
├── 📂 docs/                                 ← 📖 Documentation Suite
│   ├── ARCHITECTURE.md                      ← System design, layers, ADRs
│   ├── API_DOCS.md                          ← REST endpoint reference + cURL examples
│   ├── DB_SCHEMA.md                         ← ER diagram, SQL DDL, normalization
│   ├── JPA_ENTITIES.md                      ← Entity mapping, repositories, Lombok
│   ├── FRONTEND_DOCS.md                     ← Angular components, routing, services
│   ├── FRONTEND_INTEGRATION.md              ← Data flows, session management, interfaces
│   └── CI_PIPELINE.md                       ← GitHub Actions pipeline explained
│
├── .github/workflows/                       ← CI/CD pipeline YAML
└── README.md                                ← 📍 You are here
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

> Tables (`users`, `virtual_cards`, `transactions`) are auto-created by Hibernate on first run via `ddl-auto=update`.

### 3. Start the Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

> **Linux/macOS:** Use `./mvnw spring-boot:run`  
> Backend starts at **http://localhost:8080**

### 4. Start the Frontend

```powershell
cd frontend
npm install
npx ng serve
```

> Frontend starts at **http://localhost:4200**

### 5. Verify It Works

```bash
# Register a user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","email":"john@example.com","password":"SecurePass123"}'
# → 201 Created: {"message":"User registered successfully","userId":1}

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"SecurePass123"}'
# → 200 OK: {"userId":1,"username":"johndoe","email":"john@example.com","message":"Login successful"}

# Create a card with vendor name
curl -X POST http://localhost:8080/api/cards \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"dailyLimit":500.00,"vendorName":"Amazon"}'
# → 201 Created: {card details with generated cardNumber and cvv}
```

---

## 📡 API Quick Reference

| Method | Endpoint | Description | Response |
|:------:|----------|-------------|:--------:|
| `POST` | `/api/auth/register` | Register a new user | `201` |
| `POST` | `/api/auth/login` | Authenticate user | `200` / `401` |
| `GET` | `/api/cards/user/{userId}` | Get user's virtual cards | `200` |
| `POST` | `/api/cards` | Create a virtual card (with vendorName) | `201` |
| `PUT` | `/api/cards/{id}/toggle` | Freeze ↔ Unfreeze a card | `200` |
| `DELETE` | `/api/cards/{id}` | Permanently delete a card | `204` |
| `POST` | `/api/transactions` | Simulate a purchase | `200` / `422` |
| `GET` | `/api/transactions/card/{cardId}` | Get transaction history | `200` |

> 📖 Full API documentation with request/response examples: [docs/API_DOCS.md](docs/API_DOCS.md)

---

## 📖 Documentation Suite

| Document | Description | Link |
|----------|-------------|:----:|
| 🏗️ **Architecture** | System design, tech stack, 3-layer pattern, request lifecycle, ADRs | [ARCHITECTURE.md](docs/ARCHITECTURE.md) |
| 📡 **REST API** | All 8 endpoints, HTTP methods, status codes, DTOs, cURL examples | [API_DOCS.md](docs/API_DOCS.md) |
| 🗄️ **Database Schema** | ER diagram, 3 tables, SQL DDL, normalization, ACID theory | [DB_SCHEMA.md](docs/DB_SCHEMA.md) |
| 💾 **JPA Entities** | ORM theory, 3 entities, repositories, Lombok, derived queries | [JPA_ENTITIES.md](docs/JPA_ENTITIES.md) |
| 🅰️ **Frontend** | Angular standalone components, SPA theory, routing, services | [FRONTEND_DOCS.md](docs/FRONTEND_DOCS.md) |
| 🔗 **Integration** | End-to-end data flows, session management, TS interfaces | [FRONTEND_INTEGRATION.md](docs/FRONTEND_INTEGRATION.md) |
| ⚡ **CI/CD Pipeline** | GitHub Actions, Maven lifecycle, caching, branch protection | [CI_PIPELINE.md](docs/CI_PIPELINE.md) |

---

## 🎓 Interview Study Quick-Reference

> Key architectural decisions and their **"why"** — the questions interviewers love to ask:

| Decision | Why? |
|----------|------|
| **DTOs instead of exposing @Entity** | Prevents over-posting attacks, decouples API contract from DB schema, hides sensitive fields (passwordHash, raw FK) |
| **BCrypt for passwords** | Adaptive work-factor hashing — slows brute-force; 60-char output ensures consistent column size |
| **`@Transactional` on service methods** | Ensures atomicity — if toggle or transaction fails mid-way, changes are rolled back |
| **Standalone Components (no NgModules)** | Each component declares its own imports — tree-shakeable, self-documenting, no shared module bloat |
| **`provideHttpClient(withFetch())`** | Uses native Fetch API for HTTP — lighter than XMLHttpRequest, but requires manual `ChangeDetectorRef.detectChanges()` |
| **`CanActivateFn` guard** | Functional API (Angular 17+) — simpler than class-based guards, auto-injectable via `inject()` |
| **PUT for toggle, DELETE for remove** | Follows REST semantics — PUT modifies state, DELETE removes the resource; idempotent operations |
| **`BigDecimal` for money** | Avoids floating-point precision bugs (0.1 + 0.2 ≠ 0.3 in `double`); exact arithmetic for financial data |
| **`@Enumerated(STRING)`** | Stores enum name ("ACTIVE") not ordinal (0) — safe for future enum reordering |
| **`CascadeType.ALL` + `orphanRemoval`** | Deleting a user/card auto-cascades to children — no orphaned rows, no manual cleanup |

---

## 🗺️ Sprint History

| Sprint | Focus | Status |
|:------:|-------|:------:|
| **Sprint 1** | Project setup, DB schema, JPA entities, REST registration, basic Angular UI, CI pipeline | ✅ Complete |
| **Sprint 2** | Login flow, route guards, card CRUD (create + fetch), session management | ✅ Complete |
| **Sprint 3** | Transaction simulator, approve/decline engine, transaction history, dashboard enhancements | ✅ Complete |
| **Hardening** | Vendor name field, freeze/unfreeze toggle, delete card, dedicated `/simulator` route, UX polish | ✅ Complete |
| **Next** | SDET testing suite (JUnit, Mockito, Selenium, RestAssured) | 📋 Planned |

---

## 👥 Team & Project Management

| Aspect | Details |
|--------|---------|
| **Methodology** | Agile Scrum |
| **Sprint Duration** | 2 weeks |
| **Project Board** | Jira with Smart Commits |
| **Version Control** | GitHub (private enterprise repo) |
| **IDE** | VS Code with Copilot + Atlassian extensions |

---

<p align="center">
  <b>💳 FinVault — Smart-Card Budgeting System</b><br>
  <sub>Built with Spring Boot 4 + Angular 21 + MySQL 8.0</sub><br>
  <sub>SDET Final Training Project © 2026</sub>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Angular-21.2-DD0031?style=for-the-badge&logo=angular&logoColor=white" />
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
| 1 | [Architecture Pattern — Client-Server](#-architecture-pattern--client-server) | Why FinVault uses a decoupled design |
| 2 | [Technology Stack](#-technology-stack) | Languages, frameworks, and tools used |
| 3 | [High-Level System Design](#-high-level-system-design) | Visual diagram of the full system |
| 4 | [The 3-Layer Backend Pattern](#-the-3-layer-backend-pattern) | Controller → Service → Repository explained |
| 5 | [Request-Response Lifecycle](#-request-response-lifecycle) | Step-by-step flow of every HTTP request |
| 6 | [Frontend Architecture](#-frontend-architecture) | Angular standalone components + routing |
| 7 | [Directory Structure](#-directory-structure) | Complete file tree of the monorepo |
| 8 | [Key Architectural Decisions (ADRs)](#-key-architectural-decisions-adrs) | Design choices and their rationale |
| 9 | [Glossary](#-glossary) | Key terms and definitions |

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
| **Communicates via** | HTTP requests (GET, POST, PUT, DELETE) | JSON responses |
| **Can be deployed** | CDN, Nginx, Vercel | AWS EC2, Docker, Kubernetes |

### 🎓 Why This Pattern? (Interview Answer)

> ✅ **Independent Development** — Frontend and backend teams can work in parallel  
> ✅ **Independent Deployment** — Deploy the Angular app on a CDN, the API on a server  
> ✅ **Independent Scaling** — Scale the backend horizontally without touching the frontend  
> ✅ **Technology Freedom** — Either side can be rewritten without affecting the other  
> ✅ **Testability** — Backend can be tested via cURL/Postman without a UI  

---

## 🛠️ Technology Stack

### Backend Technologies

| Layer | Technology | Version | Purpose |
|:-----:|:-----------|:-------:|---------|
| 🔤 Language | **Java** | 21 (LTS) | Industry-standard, strongly typed, enterprise-grade |
| 🚀 Framework | **Spring Boot** | 4.0.6 | Auto-configuration, embedded Tomcat, dependency injection |
| 💾 ORM | **Spring Data JPA + Hibernate** | 7.x | Object-Relational Mapping — Java objects ↔ SQL tables |
| 🗄️ Database | **MySQL** | 8.0 | Open-source RDBMS with ACID transactions |
| 🔒 Security | **Spring Security** | — | BCrypt hashing, CORS, filter chain (JWT-ready) |
| 📦 Build Tool | **Maven** (via Maven Wrapper) | 3.x | Dependency management, build lifecycle |
| 🧹 Boilerplate | **Lombok** | — | `@Data`, `@RequiredArgsConstructor`, `@Slf4j` |

### Frontend Technologies

| Layer | Technology | Version | Purpose |
|:-----:|:-----------|:-------:|---------|
| 🅰️ Framework | **Angular** | 21.2 | Component-based SPA framework by Google |
| 📝 Language | **TypeScript** | 5.9 | Typed superset of JavaScript |
| 🎨 UI Library | **Bootstrap** | 5.3 | Responsive CSS framework with pre-built components |
| 🔄 Reactive | **RxJS** | 7.8 | Observable-based HTTP and event handling |
| 📦 Package Manager | **npm** | 11.x | Node.js package manager |

### DevOps & Tooling

| Tool | Purpose |
|------|---------|
| 🔀 **Git + GitHub** | Version control (private enterprise repo) |
| ⚡ **GitHub Actions** | CI/CD — auto-build on every push/PR to `main` |
| 📋 **Jira (Scrum)** | Sprint planning, backlog management, Smart Commits |
| 💻 **VS Code** | IDE with Copilot + Atlassian extensions |

---

## 🗺️ High-Level System Design

```
╔═══════════════════════════════════════════════════════════════════════╗
║                        🌐 CLIENT TIER                                ║
║                                                                       ║
║   ┌───────────────────────────────────────────────────────────────┐   ║
║   │              Angular 21  (localhost:4200)                     │   ║
║   │                                                               │   ║
║   │   ┌─────────────┐  ┌────────────┐  ┌──────────────────────┐  │   ║
║   │   │ Components  │  │  Services  │  │  Angular Router      │  │   ║
║   │   │ • Login     │  │ • AuthSvc  │  │  app.routes.ts       │  │   ║
║   │   │ • Dashboard │  │ • CardSvc  │  │  /login              │  │   ║
║   │   │ • Simulator │  │ • HttpClient│ │  /dashboard (guarded)│  │   ║
║   │   └─────────────┘  └─────┬──────┘  │  /simulator (guarded)│  │   ║
║   │                           │         └──────────────────────┘  │   ║
║   └───────────────────────────│───────────────────────────────────┘   ║
║                               │                                        ║
║                               │  HTTP / JSON (REST API)                ║
║                               │  CORS: localhost:4200 → localhost:8080  ║
╠═══════════════════════════════╪════════════════════════════════════════╣
║                        🖥️ SERVER TIER                                 ║
║                               │                                        ║
║   ┌───────────────────────────│────────────────────────────────────┐   ║
║   │          Spring Boot 4  (localhost:8080)                       │   ║
║   │                           │                                    │   ║
║   │   ┌───────────────────┐  ┌▼──────────────┐  ┌──────────────┐  │   ║
║   │   │  🎯 Controllers    │→│ ⚙️ Services     │→│ 💾 Repositories│ │   ║
║   │   │  AuthController   │  │ UserService   │  │ UserRepo     │  │   ║
║   │   │  CardController   │  │ CardService   │  │ CardRepo     │  │   ║
║   │   │  TransactionCtrl  │  │ TransactionSvc│  │ TransRepo    │  │   ║
║   │   └───────────────────┘  └───────────────┘  └──────┬───────┘  │   ║
║   │                                                     │ JPA/SQL  │   ║
║   │   ┌─────────────────────────────────────────────────│───────┐  │   ║
║   │   │     🔒 Spring Security Filter Chain             │       │  │   ║
║   │   │     BCryptPasswordEncoder + CORS + permitAll()  │       │  │   ║
║   │   └─────────────────────────────────────────────────│───────┘  │   ║
║   └─────────────────────────────────────────────────────│──────────┘   ║
║                                                         │              ║
╠═════════════════════════════════════════════════════════╪══════════════╣
║                        🗄️ DATA TIER                     │              ║
║                                                         │              ║
║   ┌─────────────────────────────────────────────────────│──────────┐   ║
║   │              MySQL 8.0  (port 3306)                 ▼          │   ║
║   │              Database: finvault_db                              │   ║
║   │                                                                │   ║
║   │   ┌──────────┐  1:M  ┌────────────────┐  1:M  ┌────────────┐ │   ║
║   │   │  users   │──────►│ virtual_cards  │──────►│transactions│ │   ║
║   │   │          │       │                │       │            │ │   ║
║   │   └──────────┘       └────────────────┘       └────────────┘ │   ║
║   │                                                                │   ║
║   └────────────────────────────────────────────────────────────────┘   ║
╚═══════════════════════════════════════════════════════════════════════╝
```

---

## 🧱 The 3-Layer Backend Pattern

FinVault's backend follows a strict **3-Layer Architecture** — the most common pattern in enterprise Java:

```
        ┌──────────────────────────────────────────────────────┐
        │                  📨 CONTROLLER LAYER                  │
        │    Receives HTTP requests, validates, returns responses│
        │    AuthController · VirtualCardController              │
        │    TransactionController                              │
        └──────────────────────────┬───────────────────────────┘
                                   │  calls ↓
        ┌──────────────────────────▼───────────────────────────┐
        │                  ⚙️ SERVICE LAYER                      │
        │    Contains ALL business logic + @Transactional       │
        │    UserService · VirtualCardService                    │
        │    TransactionService (daily-limit check)             │
        └──────────────────────────┬───────────────────────────┘
                                   │  calls ↓
        ┌──────────────────────────▼───────────────────────────┐
        │                  💾 REPOSITORY LAYER                   │
        │    Talks to MySQL via JPA — zero SQL required         │
        │    UserRepository · VirtualCardRepository             │
        │    TransactionRepository                              │
        └──────────────────────────────────────────────────────┘
```

### 🎓 Why 3 Layers? (Interview Answer)

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

### Example: User Registration Flow

```
 👤 User                   🌐 Angular               🖥️ Spring Boot           🗄️ MySQL
  │                          │                          │                       │
  │  1. Fills form           │                          │                       │
  │  & clicks Register       │                          │                       │
  │─────────────────────────►│                          │                       │
  │                          │  2. AuthService.register()│                      │
  │                          │  POST /api/auth/register │                       │
  │                          │─────────────────────────►│                       │
  │                          │                          │  3. SecurityFilter    │
  │                          │                          │  permitAll() — passes │
  │                          │                          │                       │
  │                          │                          │  4. AuthController    │
  │                          │                          │  @PostMapping         │
  │                          │                          │  calls UserService    │
  │                          │                          │                       │
  │                          │                          │  5. UserService       │
  │                          │                          │  existsByEmail check  │
  │                          │                          │  BCrypt.encode()      │
  │                          │                          │                       │
  │                          │                          │  6. UserRepository    │
  │                          │                          │  .save(user)          │
  │                          │                          │──────────────────────►│
  │                          │                          │  7. INSERT INTO users │
  │                          │                          │◄──────────────────────│
  │                          │                          │                       │
  │                          │  8. 201 Created          │                       │
  │                          │  { message, userId }     │                       │
  │                          │◄─────────────────────────│                       │
  │  9. setSession() +       │                          │                       │
  │  router → /dashboard     │                          │                       │
  │◄─────────────────────────│                          │                       │
```

### Example: Transaction Approval Flow

```
 Dashboard               VirtualCardService         TransactionService        MySQL
    │                          │                          │                    │
    │  simulatePurchase()      │                          │                    │
    │  POST /api/transactions  │                          │                    │
    │─────────────────────────────────────────────────────►                    │
    │                          │                          │  findById(cardId)  │
    │                          │                          │───────────────────►│
    │                          │                          │◄───────────────────│
    │                          │                          │                    │
    │                          │  if (balance + amount <= dailyLimit)          │
    │                          │         → status = SUCCESS                    │
    │                          │         → card.balance += amount              │
    │                          │  else                                         │
    │                          │         → status = DECLINED                   │
    │                          │                          │                    │
    │                          │                          │  save(transaction) │
    │                          │                          │───────────────────►│
    │                          │                          │◄───────────────────│
    │                          │                          │                    │
    │  200 OK (SUCCESS)        │                          │                    │
    │  or 422 (DECLINED)       │                          │                    │
    │◄─────────────────────────────────────────────────────                    │
```

---

## 🅰️ Frontend Architecture

### Standalone Components (Angular 17+ Pattern)

FinVault uses **standalone components** — the modern Angular pattern that eliminates NgModules:

```typescript
@Component({
  selector: 'app-dashboard',
  standalone: true,                      // ← Self-contained, no NgModule required
  imports: [CommonModule, FormsModule],  // ← Declares its own dependencies
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent { }
```

### 🎓 Why Standalone? (Interview Answer)

> Each component declares exactly what it needs — no bloated `AppModule` with 200 imports. This pattern enables **tree-shaking** (unused components are excluded from the build), makes dependencies **self-documenting**, and aligns with Angular's future direction.

### App Routes

```typescript
export const routes: Routes = [
  { path: '',           redirectTo: 'login', pathMatch: 'full' },
  { path: 'login',      component: LoginComponent },
  { path: 'dashboard',  component: DashboardComponent,  canActivate: [authGuard] },
  { path: 'simulator',  component: SimulatorComponent,  canActivate: [authGuard] },
  { path: '**',         redirectTo: 'login' }
];
```

### Session Management

```
Login → tap() → sessionStorage.setItem('finvault_user', JSON.stringify({userId, username, email}))
Guard → sessionStorage.getItem('finvault_user') → null? redirect to /login : allow
Logout → sessionStorage.removeItem() + router.navigate(['/login'])
```

---

## 📂 Directory Structure

```
FinVault/
│
├── 📂 backend/                                    ← Spring Boot application
│   ├── src/main/java/com/finvault/backend/
│   │   ├── BackendApplication.java                ← @SpringBootApplication entry point
│   │   ├── config/SecurityConfig.java             ← BCrypt + CORS + SecurityFilterChain
│   │   ├── controller/
│   │   │   ├── AuthController.java                ← /api/auth/register, /api/auth/login
│   │   │   ├── VirtualCardController.java         ← /api/cards (CRUD + toggle)
│   │   │   └── TransactionController.java         ← /api/transactions (simulate + history)
│   │   ├── dto/                                   ← 7 Data Transfer Objects
│   │   ├── entity/                                ← 3 JPA entities (User, VirtualCard, Transaction)
│   │   ├── repository/                            ← 3 Spring Data JPA interfaces
│   │   └── service/                               ← 3 business logic services
│   └── src/main/resources/application.properties  ← MySQL + JPA + logging config
│
├── 📂 frontend/src/app/                           ← Angular application
│   ├── app.ts / app.html / app.routes.ts          ← Root shell + routing
│   ├── app.config.ts                              ← provideRouter + provideHttpClient(withFetch())
│   ├── guards/auth.guard.ts                       ← CanActivateFn route protection
│   ├── services/                                  ← AuthService + VirtualCardService
│   ├── login/                                     ← Login + Signup component
│   ├── dashboard/                                 ← 3-tab card dashboard
│   └── simulator/                                 ← QA transaction testing tool
│
└── 📂 docs/                                       ← 7 documentation files
```

---

## 🏛️ Key Architectural Decisions (ADRs)

> **ADR** = Architecture Decision Record — documenting the **"why"** behind every choice.

| # | Decision | Choice | Rationale (Interview-Ready) |
|:-:|----------|--------|----------------------------|
| 1 | **Architecture style** | Decoupled Client-Server | Independent deployability; Angular on CDN, API on a server; frontend can be rewritten without touching backend |
| 2 | **Password storage** | BCrypt (strength 10) | Adaptive work-factor hashing; brute-force resistant; 60-char fixed output; industry standard |
| 3 | **ORM strategy** | Spring Data JPA + Hibernate | Zero-boilerplate CRUD; derived query methods; schema auto-migration via `ddl-auto=update` |
| 4 | **API data contract** | DTOs (not raw entities) | Prevents over-posting, hides sensitive fields (passwordHash, internal FKs), decouples API evolution from DB schema |
| 5 | **HTTP method for toggle** | `PUT /cards/{id}/toggle` | PUT is idempotent for state change; body-less because the target state is inferred (flip ACTIVE↔FROZEN) |
| 6 | **HTTP method for delete** | `DELETE /cards/{id}` | REST semantic — DELETE removes a resource permanently; returns 204 No Content |
| 7 | **Money representation** | `BigDecimal` | Prevents IEEE 754 floating-point errors (0.1+0.2≠0.3); exact arithmetic for financial calculations |
| 8 | **Enum persistence** | `@Enumerated(EnumType.STRING)` | Stores "ACTIVE" not 0 — safe for future enum reordering; human-readable in raw SQL queries |
| 9 | **Cascading deletes** | `CascadeType.ALL` + `orphanRemoval` | Deleting a user cascades to cards; deleting a card cascades to transactions — no orphaned rows |
| 10 | **Angular HTTP provider** | `provideHttpClient(withFetch())` | Uses native Fetch API; lighter than XHR; requires explicit `ChangeDetectorRef.detectChanges()` |
| 11 | **Route protection** | `CanActivateFn` (functional guard) | Simpler than class-based guards; uses `inject()` for DI; Angular 17+ recommended pattern |
| 12 | **Session storage** | `sessionStorage` | Simpler than JWT for training scope; auto-clears on tab close; sufficient for single-tab prototype |
| 13 | **CSS framework** | Bootstrap 5 via `angular.json` styles array | Global availability; no per-component imports; proven responsive grid system |
| 14 | **Card vendor field** | `vendorName` (VARCHAR 100, DEFAULT '') | Human-readable label for budgeting context; backward-compatible with existing rows via DEFAULT |
| 15 | **Build tool** | Maven Wrapper (`mvnw`) | Zero-install builds — CI and developers use the same Maven version without manual installation |
| 16 | **Repository strategy** | Monorepo | Single PR covers full-stack changes; simplified Jira Smart Commit traceability |
| 17 | **CORS configuration** | Explicit allowedOrigins: `localhost:4200` | Security best practice — only the known frontend origin can make cross-origin requests |
| 18 | **CSRF disabled** | `csrf.disable()` | API is stateless (no session cookies); Angular sends JSON via HttpClient, not HTML form posts |

---

## 📚 Glossary

| Term | Definition |
|------|-----------|
| **SPA** | Single Page Application — loads one HTML page, Angular handles navigation dynamically |
| **REST** | Representational State Transfer — API style using standard HTTP methods |
| **DTO** | Data Transfer Object — plain class for transferring data between layers |
| **BCrypt** | Adaptive password hashing algorithm with configurable work factor |
| **CORS** | Cross-Origin Resource Sharing — HTTP headers that permit cross-domain requests |
| **CSRF** | Cross-Site Request Forgery — attack vector disabled because FinVault is stateless |
| **JPA** | Java Persistence API — specification for ORM in Java |
| **ORM** | Object-Relational Mapping — bridges Java objects and SQL tables |
| **ADR** | Architecture Decision Record — documents the "why" behind a design choice |
| **Monorepo** | Single Git repository containing multiple projects (frontend + backend) |
| **Standalone Component** | Angular component that declares its own imports without NgModules |
| **CanActivateFn** | Angular functional route guard (replaces class-based `CanActivate`) |

---

<p align="center">
  <b>📐 FinVault Architecture Document</b><br>
  <sub>Part of the <a href="../README.md">FinVault Documentation Suite</a></sub>
</p>

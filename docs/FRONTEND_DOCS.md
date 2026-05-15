<p align="center">
  <img src="https://img.shields.io/badge/Angular-21-DD0031?style=for-the-badge&logo=angular&logoColor=white" />
  <img src="https://img.shields.io/badge/TypeScript-5.x-3178C6?style=for-the-badge&logo=typescript&logoColor=white" />
  <img src="https://img.shields.io/badge/Bootstrap-5-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white" />
  <img src="https://img.shields.io/badge/Standalone-Components-FF4081?style=for-the-badge" />
</p>

# 🅰️ FinVault — Frontend Documentation

> **Tickets:** SCRUM-15, SCRUM-16, SCRUM-17, SCRUM-18 | **Framework:** Angular 21 (Standalone Components)  
> **Styling:** Bootstrap 5 | **HTTP Client:** Angular `HttpClient` (`provideHttpClient(withFetch())`)

---

## 📑 Table of Contents

| # | Section | Description |
|:-:|---------|-------------|
| 1 | [What is Angular? — Theory](#-what-is-angular--theory) | Core concepts of the Angular framework |
| 2 | [What is a SPA?](#-what-is-a-spa) | Single Page Application architecture explained |
| 3 | [Standalone Components (Angular 17+)](#-standalone-components-angular-17) | The modern Angular pattern — no NgModules |
| 4 | [Project Structure](#-project-structure) | File tree with descriptions |
| 5 | [Application Bootstrap Flow](#-application-bootstrap-flow) | How Angular starts up |
| 6 | [Routing Configuration](#-routing-configuration) | URL → Component mapping |
| 7 | [Services & Dependency Injection](#-services--dependency-injection) | HttpClient and Angular DI |
| 8 | [Component Deep Dive: LoginComponent](#-component-deep-dive-logincomponent) | Registration form — code walkthrough |
| 9 | [Component Deep Dive: DashboardComponent](#-component-deep-dive-dashboardcomponent) | Card dashboard — layout and mock data |
| 10 | [Angular ↔ Spring Boot Communication](#-angular--spring-boot-communication) | How frontend talks to backend |
| 11 | [Bootstrap 5 Integration](#-bootstrap-5-integration) | CSS framework setup |
| 12 | [Key Angular Concepts Used](#-key-angular-concepts-used) | Directives, bindings, forms |
| 13 | [Glossary](#-glossary) | Key frontend terms |

---

## 📖 What is Angular? — Theory

### Angular — A Component-Based Framework

**Angular** is a TypeScript-based frontend framework developed and maintained by **Google**. It's used to build **Single Page Applications (SPAs)** — web apps that feel as fast as desktop apps.

> 💡 **Think of Angular like LEGO blocks:**  
> Each component is a self-contained LEGO brick (with its own HTML, CSS, and logic).  
> You snap them together to build the full application.  
> You can swap, reuse, or replace any brick without rebuilding the entire structure.

### Angular's Core Building Blocks

| Concept | What It Is | Analogy | FinVault Example |
|---------|-----------|---------|------------------|
| **Component** | A reusable UI unit with its own template, styles, and logic | A LEGO brick | `LoginComponent`, `DashboardComponent` |
| **Template** | The HTML that defines what the component renders | The shape of the brick | `login.component.html` |
| **Service** | A class that handles data, HTTP calls, or shared logic | A delivery truck that brings data to bricks | `AuthService` |
| **Router** | Maps URLs to components — enables navigation without page reload | Road signs that direct traffic | `app.routes.ts` |
| **Directive** | Special instructions in HTML that modify the DOM | Remote controls for bricks | `@if`, `@for`, `ngModel` |
| **Module** | ~~A container for organizing components~~ | **Replaced by Standalone Components in Angular 17+** | Not used in FinVault |

### Why Angular Over React or Vue?

| Feature | Angular | React | Vue |
|---------|:-------:|:-----:|:---:|
| Language | TypeScript (built-in) | JavaScript (TS optional) | JavaScript (TS optional) |
| Architecture | Full framework (routing, HTTP, forms included) | Library (need 3rd party) | Progressive framework |
| Learning Curve | Steeper | Moderate | Gentle |
| Enterprise Adoption | Very high (Google, Microsoft) | Very high (Meta, Netflix) | Growing (Alibaba, GitLab) |
| FinVault Choice | ✅ **Selected** | — | — |

> 🎯 **Why Angular for FinVault?** It's a full framework with built-in solutions for routing, HTTP, forms, and dependency injection — no need to piece together multiple libraries. Perfect for a structured enterprise financial application.

---

## 🌐 What is a SPA?

### Single Page Application — How It Works

A **SPA** (Single Page Application) loads a **single HTML page** on the first visit, then dynamically updates the content using JavaScript — without ever reloading the page.

```
        TRADITIONAL WEB APP (Multi-Page)              SPA (Single-Page — Angular)
  ┌─────────────────────────────────┐        ┌─────────────────────────────────┐
  │                                 │        │                                 │
  │  Click "Dashboard" →            │        │  Click "Dashboard" →            │
  │  Browser requests NEW page      │        │  Angular swaps component        │
  │  Server sends FULL HTML         │        │  No server round-trip!          │
  │  White flash while loading      │        │  Instant, seamless transition   │
  │  ⏱️ 500ms-2000ms               │        │  ⏱️ ~10ms                       │
  │                                 │        │                                 │
  │  Click "Cards" →                │        │  Click "Cards" →                │
  │  Another FULL page reload       │        │  Router swaps component again   │
  │  ⏱️ 500ms-2000ms again         │        │  ⏱️ ~10ms again                 │
  │                                 │        │                                 │
  └─────────────────────────────────┘        └─────────────────────────────────┘
```

### How FinVault's SPA Works

```
  1. First Visit → Browser downloads index.html + main.js (Angular bundle)
  2. Angular boots up and renders <router-outlet /> in app.html
  3. URL is "/login" → Router loads LoginComponent into the outlet
  4. User registers → Router navigates to "/dashboard"
  5. DashboardComponent replaces LoginComponent — NO page reload!
```

---

## ⚡ Standalone Components (Angular 17+)

### The Old Way vs The New Way

```
  ❌ OLD: NgModule-based (Angular 2-16)
  ──────────────────────────────────────
  @NgModule({
    declarations: [LoginComponent, DashboardComponent],
    imports: [CommonModule, FormsModule, RouterModule],
    providers: [AuthService],
    bootstrap: [AppComponent]
  })
  export class AppModule {}     ← Extra boilerplate file!

  ✅ NEW: Standalone Components (Angular 17+ / FinVault)
  ────────────────────────────────────────────────────────
  @Component({
    selector: 'app-login',
    standalone: true,                    ← Component manages its own imports
    imports: [CommonModule, FormsModule], ← Declares exactly what it needs
    templateUrl: './login.component.html'
  })
  export class LoginComponent {}         ← No NgModule needed!
```

> 💡 **Key benefit:** Each component is **self-contained** — it declares its own dependencies. No more shared `AppModule` that grows into a 200-line import list.

---

## 📂 Project Structure

```
frontend/src/
│
├── 📄 index.html                              ← SPA shell — single HTML page
│   └── Contains <app-root></app-root> tag where Angular mounts
│
├── 📄 main.ts                                 ← 🚀 Bootstrap entry point
│   └── bootstrapApplication(App, appConfig)
│
├── 📄 styles.css                              ← 🎨 Global CSS styles
│
└── 📂 app/
    │
    ├── 📄 app.ts                              ← 🅰️ Root Component (shell)
    │   └── Contains <router-outlet /> — placeholder for routed components
    │
    ├── 📄 app.html                            ← Root template: just <router-outlet />
    │
    ├── 📄 app.css                             ← Root component styles (minimal)
    │
    ├── 📄 app.routes.ts                       ← 🛤️ Route definitions
    │   └── Maps URLs to components:
    │       "/" → redirect to "/login"
    │       "/login" → LoginComponent
    │       "/dashboard" → DashboardComponent + canActivate: [authGuard]
    │       "/**" → redirect to "/login"
    │
    ├── 📄 app.config.ts                       ← ⚙️ Application-wide providers
    │   └── provideRouter(routes)
    │   └── provideHttpClient(withFetch())
    │
    ├── 📂 guards/
    │   └── 📄 auth.guard.ts                   ← 🔒 Blocks /dashboard when not logged in
    │       └── Calls authService.isLoggedIn() → redirects to /login if false
    │
    ├── 📂 services/
    │   ├── 📄 auth.service.ts                 ← 📡 Auth HTTP + sessionStorage session
    │   │   ├── register(payload) → POST /api/auth/register
    │   │   ├── login(payload)    → POST /api/auth/login + auto setSession()
    │   │   ├── setSession(user)  → writes to sessionStorage
    │   │   ├── getSession()      → reads from sessionStorage
    │   │   ├── isLoggedIn()      → checks sessionStorage key presence
    │   │   └── logout()          → clears sessionStorage + navigates to /login
    │   │
    │   └── 📄 virtual-card.service.ts        ← 📡 HTTP calls to /api/cards + /api/transactions
    │       ├── getCardsByUserId(userId)
    │       ├── createCard(userId, dailyLimit)
    │       ├── processTransaction(cardId, amount, merchantName)
    │       └── getTransactionsByCardId(cardId)
    │
    ├── 📂 login/
    │   ├── 📄 login.component.ts              ← 📝 Login + Signup tab logic
    │   │   ├── activeTab: 'login' | 'signup'
    │   │   ├── onLogin() → AuthService.login() → setSession() → /dashboard
    │   │   └── onSignup() → AuthService.register() → setSession() → /dashboard
    │   └── 📄 login.component.html            ← 🎨 Bootstrap card with Sign In / Create Account tabs
    │       └── Template-driven form with validation + alerts
    │
    └── 📂 dashboard/
        ├── 📄 dashboard.component.ts          ← 📊 Real API calls, 3-tab sidebar, forkJoin transactions
        │   ├── cards: VirtualCard[], loading: boolean
        │   ├── transactions: TransactionResponse[], txLoading: boolean
        │   ├── creatingCard: boolean, processingCardId: number | null
        │   ├── activeNav: 'dashboard' | 'cards' | 'transactions'
        │   ├── setActiveNav(nav) → triggers fetchAllTransactions() for tx tab
        │   ├── fetchCards() → GET /api/cards/user/{userId}
        │   ├── generateCard() → POST /api/cards
        │   ├── simulatePurchase(cardId, event) → POST /api/transactions
        │   ├── fetchAllTransactions() → forkJoin(...getTransactionsByCardId)
        │   ├── DestroyRef + takeUntilDestroyed (prevents memory leaks)
        │   └── ChangeDetectorRef.detectChanges() (forces re-render with withFetch())
        └── 📄 dashboard.component.html        ← 🎨 Sidebar + Dashboard/My Cards/Transactions tabs
```

---

## 🚀 Application Bootstrap Flow

Here's the step-by-step process of how Angular starts:

```
  📄 index.html loads in browser
      │
      │  Contains: <app-root></app-root>
      │  Includes: <script src="main.js"></script>
      ▼
  📄 main.ts executes
      │
      │  bootstrapApplication(App, appConfig)
      │  ← Boots Angular with root component + providers
      ▼
  📄 app.config.ts provides:
      │
      ├── provideRouter(routes)          ← Enables Angular Router
      ├── provideHttpClient(withFetch()) ← Enables HttpClient for API calls
      └── provideBrowserGlobalErrorListeners()
      │
      ▼
  📄 app.ts (Root Component) renders app.html
      │
      │  Template: <router-outlet />
      │  ← This is WHERE routed components appear
      ▼
  📄 app.routes.ts evaluates current URL
      │
      ├── URL = "/"          → redirectTo "/login"
      ├── URL = "/login"     → Load LoginComponent
      ├── URL = "/dashboard" → Load DashboardComponent
      └── URL = "/anything"  → redirectTo "/login"
      │
      ▼
  🎯 LoginComponent renders inside <router-outlet />
```

---

## 🛤️ Routing Configuration

### Route Table: `app.routes.ts`

```typescript
export const routes: Routes = [
  { path: '',          redirectTo: 'login', pathMatch: 'full' },
  { path: 'login',    component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: '**',       redirectTo: 'login' }
];
```

### Route Breakdown

| Path | Component | Guard | Behavior | URL Example |
|:----:|:---------:|:-----:|----------|:-----------:|
| `''` | — | — | Redirects to `/login` (home page) | `localhost:4200/` |
| `'login'` | `LoginComponent` | — | Shows login/signup tabs | `localhost:4200/login` |
| `'dashboard'` | `DashboardComponent` | `authGuard` | Shows card management dashboard — **blocked if not logged in** | `localhost:4200/dashboard` |
| `'**'` | — | — | Wildcard: catches all unknown URLs → redirects to `/login` | `localhost:4200/anything` |

### What is `<router-outlet />`?

```html
<!-- app.html -->
<router-outlet />
```

> 💡 `<router-outlet />` is a **placeholder** in the root component's template.  
> Angular's Router looks at the current URL and **swaps in** the matching component:

```
  URL: /login                          URL: /dashboard
  ┌─────────────────────┐              ┌─────────────────────┐
  │ <app-root>          │              │ <app-root>          │
  │   <router-outlet /> │              │   <router-outlet /> │
  │   ┌───────────────┐ │              │   ┌───────────────┐ │
  │   │ LoginComponent│ │   navigate   │   │  Dashboard    │ │
  │   │ (form)        │ │ ──────────►  │   │  Component    │ │
  │   └───────────────┘ │              │   │  (cards)      │ │
  │                     │              │   └───────────────┘ │
  └─────────────────────┘              └─────────────────────┘
```

---

## 💉 Services & Dependency Injection

### What is Dependency Injection (DI)?

**DI** is a design pattern where a class **receives** its dependencies from the outside instead of creating them itself:

```typescript
// ❌ Without DI — tightly coupled
class LoginComponent {
  private authService = new AuthService(new HttpClient(...));  // Creates own dependency!
}

// ✅ With DI — Angular injects automatically
class LoginComponent {
  constructor(private authService: AuthService) {}             // Angular provides it!
}
```

> 💡 Angular's DI system creates one **singleton** instance of each service and shares it across all components that request it.

### `AuthService` — HTTP Communication + Session

```typescript
@Injectable({ providedIn: 'root' })    // ← Available app-wide (singleton)
export class AuthService {

  private readonly apiUrl = 'http://localhost:8080/api/auth';
  private readonly SESSION_KEY = 'finvault_user';

  register(payload: UserRegistrationRequest): Observable<RegistrationResponse> {
    return this.http.post<RegistrationResponse>(`${this.apiUrl}/register`, payload);
  }

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, payload).pipe(
      tap(res => this.setSession({ userId: res.userId, username: res.username, email: res.email }))
    );  // ← automatically stores session on success
  }

  setSession(user: SessionUser): void {
    sessionStorage.setItem(this.SESSION_KEY, JSON.stringify(user));
  }

  getSession(): SessionUser | null {
    const raw = sessionStorage.getItem(this.SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  isLoggedIn(): boolean {
    return sessionStorage.getItem(this.SESSION_KEY) !== null;
  }

  logout(): void {
    sessionStorage.removeItem(this.SESSION_KEY);
    this.router.navigate(['/login']);
  }
}
```

### `VirtualCardService` — Cards & Transactions

```typescript
@Injectable({ providedIn: 'root' })
export class VirtualCardService {
  private readonly baseUrl = 'http://localhost:8080/api';

  getCardsByUserId(userId: number): Observable<VirtualCard[]> {
    return this.http.get<VirtualCard[]>(`${this.baseUrl}/cards/user/${userId}`);
  }

  createCard(userId: number, dailyLimit: number): Observable<VirtualCard> {
    return this.http.post<VirtualCard>(`${this.baseUrl}/cards`, { userId, dailyLimit });
  }

  processTransaction(cardId: number, amount: number, merchantName: string): Observable<TransactionResponse> {
    return this.http.post<TransactionResponse>(`${this.baseUrl}/transactions`, { cardId, amount, merchantName });
  }

  getTransactionsByCardId(cardId: number): Observable<TransactionResponse[]> {
    return this.http.get<TransactionResponse[]>(`${this.baseUrl}/transactions/card/${cardId}`);
  }
}
```

### TypeScript Interfaces (Type Safety)

```typescript
export interface UserRegistrationRequest {
  username: string;
  email: string;
  password: string;
}

export interface RegistrationResponse {
  message: string;
  userId: number;
}
```

> 💡 **Why interfaces?** TypeScript interfaces enforce the shape of data at compile time — if the API changes its response format, TypeScript catches the error immediately.

### `provideHttpClient(withFetch())` — What is `withFetch()`?

```typescript
// app.config.ts
provideHttpClient(withFetch())
```

| Option | Uses | Benefit |
|--------|------|---------|
| Default | `XMLHttpRequest` (XHR) | Legacy browser support |
| `withFetch()` ⭐ | Browser's native `fetch` API | Modern, better SSR compatibility, recommended in Angular 21 |

---

## 📝 Component Deep Dive: LoginComponent

### What It Does

A centered Bootstrap card with **two tabs** — Sign In and Create Account — that communicate with the Spring Boot backend:

```
┌──────────────────────────────────────────┐
│              🌑 Dark Background           │
│                                           │
│    ┌──────────────────────────────────┐   │
│    │   💳 FinVault                    │   │  ← Blue header
│    │   Smart-Card Budgeting System    │   │
│    ├──────────────────────────────────┤   │
│    │ [ Sign In ]   [ Create Account ] │   │  ← Tab switcher
│    ├──────────────────────────────────┤   │
│    │                                  │   │
│    │   Email:     [____________]      │   │
│    │   Password:  [____________]      │   │
│    │                                  │   │
│    │   [  Sign In  ]                  │   │  ← Calls onLogin()
│    │                                  │   │
│    └──────────────────────────────────┘   │
│                                           │
└──────────────────────────────────────────┘
```

### Component Logic — `login.component.ts`

```typescript
export class LoginComponent {

  activeTab: 'login' | 'signup' = 'login';

  // Login tab fields
  loginData: LoginRequest = { email: '', password: '' };

  // Signup tab fields
  signupData: UserRegistrationRequest = { username: '', email: '', password: '' };

  isLoading = false;
  successMessage = '';
  errorMessage = '';

  switchTab(tab: 'login' | 'signup'): void {
    this.activeTab = tab;
    this.successMessage = '';
    this.errorMessage = '';
  }

  onLogin(): void {
    this.isLoading = true;
    this.authService.login(this.loginData).subscribe({
      next: () => {
        this.isLoading = false;
        // session auto-stored by AuthService.login() via tap()
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.error || 'Invalid email or password.';
      }
    });
  }

  onSignup(): void {
    this.isLoading = true;
    this.authService.register(this.signupData).subscribe({
      next: (res) => {
        this.isLoading = false;
        // Manually set session then navigate (register doesn't auto-login)
        this.authService.setSession({
          userId: res.userId,
          username: this.signupData.username,
          email: this.signupData.email
        });
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.error || 'Registration failed.';
      }
    });
  }
}
```

### Form Features

| Feature | Implementation | Purpose |
|---------|---------------|---------|
| **Tab switching** | `activeTab: 'login' \| 'signup'` + `@if` blocks | Separate Sign In and Create Account forms |
| **Two-way binding** | `[(ngModel)]="loginData.email"` | Syncs input field ↔ TypeScript property in real-time |
| **Required validation** | `required` + `#emailField="ngModel"` | Prevents empty submissions |
| **Email format** | `type="email"` + `email` attribute | Angular validates email format |
| **Error messages** | `@if (emailField.invalid && emailField.touched)` | Shows only after user interacts |
| **Submit guard** | `[disabled]="loginForm.invalid \|\| isLoading"` | Prevents invalid/duplicate submissions |
| **Loading spinner** | `@if (isLoading)` → spinner inside button | Visual feedback during API call |

---

## 📊 Component Deep Dive: DashboardComponent

### What It Does

A full-page Bootstrap dashboard with a **sidebar** and **3 tabs** — Dashboard overview, My Cards (with generate + simulate), and Transactions history:

```
┌──────────────────────────────────────────────────────────────────────┐
│  🏦 FinVault    Smart-Card Budgeting System       [JD] │  ← Top Navbar
├────────┬─────────────────────────────────────────────────────────────┤
│        │  Welcome back, Johndoe 👋                                   │
│ 🏠     │  [Dashboard tab]                                            │
│ Dash   │                                                             │
│        │  ┌───────┐ ┌──────────┐ ┌───────────┐                      │
│ 💳     │  │Cards:2│ │Limit $1K │ │Spent $50  │                      │  ← Stats
│ Cards  │  └───────┘ └──────────┘ └───────────┘                      │
│        │                                                             │
│ 📊     │  CARDS OVERVIEW (read-only)                                 │
│ Trans  │  ┌───────────┐ ┌───────────┐                               │  ← Cards
│        │  │ **** 1234 │ │ **** 5678 │                               │
│ 🚪     │  │ $500 limit│ │ $200 limit│                               │
│ Logout │  └───────────┘ └───────────┘                               │
├────────┴─────────────────────────────────────────────────────────────┤
```

### Live API Data (No More Mock Data)

```typescript
export class DashboardComponent implements OnInit {

  cards: VirtualCard[] = [];               // Loaded from GET /api/cards/user/{userId}
  loading = false;                          // Shows spinner during fetch
  creatingCard = false;                    // Shows spinner on Generate button
  processingCardId: number | null = null;  // Per-card processing lock

  transactions: TransactionResponse[] = []; // Loaded from all cards via forkJoin
  txLoading = false;

  activeNav = 'dashboard';                 // Controls which tab is visible

  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  ngOnInit(): void {
    this.user = this.authService.getSession(); // Read from sessionStorage
    this.fetchCards();
  }

  setActiveNav(nav: string): void {
    this.activeNav = nav;
    if (nav === 'transactions') {
      this.fetchAllTransactions();   // Lazy-loads when tab is opened
    }
  }

  fetchAllTransactions(): void {
    // forkJoin fires all card requests in parallel, then merges + sorts
    const requests = this.cards.map(c =>
      this.cardService.getTransactionsByCardId(c.id)
        .pipe(catchError(() => of([])))
    );
    forkJoin(requests).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (results) => {
        this.transactions = results.flat()
          .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
        this.txLoading = false;
        this.cdr.detectChanges();    // Required: withFetch() bypasses zone.js
      }
    });
  }
}
```

### The 3 Dashboard Tabs

| Tab | Sidebar Button | What It Shows |
|-----|:---:|------|
| **Dashboard** | 🏠 | Stats row (total cards, total limit, spent today) + read-only card overview grid |
| **My Cards** | 💳 | Generate New Card form + card grid with ☕ Simulate $50 Purchase button per card |
| **Transactions** | 📊 | Full transaction history table (merchant, amount, status badge, timestamp) with Refresh button |

---

## 🔗 Angular ↔ Spring Boot Communication

### The Full Request Flow

```
  👤 User clicks "Register"
      │
      ▼
  📄 LoginComponent.onSubmit()
      │
      │  this.authService.register(this.formData)
      ▼
  📡 AuthService.register(payload)
      │
      │  this.http.post('http://localhost:8080/api/auth/register', payload)
      ▼
  🌐 Browser's Fetch API sends HTTP request
      │
      │  POST /api/auth/register
      │  Content-Type: application/json
      │  Body: { "username": "johndoe", "email": "john@example.com", "password": "..." }
      ▼
  🖥️ Spring Boot receives request (port 8080)
      │
      │  AuthController → UserService → UserRepository → MySQL
      ▼
  📨 JSON Response sent back
      │
      │  201 Created
      │  { "message": "User registered successfully", "userId": 1 }
      ▼
  📡 AuthService Observable emits response
      │
      ▼
  📄 LoginComponent.subscribe({ next: (res) => ... })
      │
      │  Shows success alert
      │  setTimeout → router.navigate(['/dashboard'])
      ▼
  🛤️ Angular Router loads DashboardComponent
```

### Observables — Angular's Async Pattern

```typescript
// AuthService returns an Observable (not a Promise!)
this.authService.register(this.formData).subscribe({
  next: (res) => { /* Success handler */ },
  error: (err) => { /* Error handler */ }
});
```

> 💡 **Observable vs Promise:**
>
> | Feature | Promise | Observable |
> |---------|:-------:|:----------:|
> | Values emitted | One | One or many |
> | Cancelable | No | Yes (`unsubscribe()`) |
> | Lazy | No (executes immediately) | Yes (executes on subscribe) |
> | Used by | `fetch()` API | Angular `HttpClient` |

---

## 🎨 Bootstrap 5 Integration

### How Bootstrap is Registered

Bootstrap CSS is registered **globally** in `angular.json`, making it available to every component:

```json
// angular.json → architect → build → options → styles
"styles": [
  "node_modules/bootstrap/dist/css/bootstrap.min.css",
  "src/styles.css"
]
```

### Why Global Registration?

| Approach | Pros | Cons | Used in FinVault? |
|----------|------|------|:-----------------:|
| Global via `angular.json` | Available everywhere; no per-component imports | Entire CSS is bundled | ✅ Yes |
| Per-component `@import` | Tree-shakeable; only loads what's needed | Must import in every component | No |

### Bootstrap Classes Used in FinVault

| Class | Purpose | Where Used |
|-------|---------|------------|
| `min-vh-100` | Full viewport height | Login page, Dashboard |
| `d-flex`, `align-items-center`, `justify-content-center` | Flexbox centering | Login card centering |
| `card`, `card-header`, `card-body`, `card-footer` | Card component | Login form, Virtual cards |
| `form-control`, `form-label` | Form styling | Registration inputs |
| `btn btn-primary` | Primary action button | Submit button |
| `alert alert-success`, `alert alert-danger` | Alert banners | Success/error messages |
| `badge bg-success` | Status badges | "Online" indicator, card statuses |
| `row`, `col-md-3`, `col-md-6`, `col-xl-3` | Responsive grid | Stats row, card grid |
| `bg-dark`, `bg-primary`, `text-white` | Color utilities | Dark theme, colored cards |
| `shadow`, `shadow-lg` | Box shadows | Cards, navbar |
| `navbar`, `navbar-dark`, `navbar-brand` | Navigation bar | Dashboard top nav |
| `spinner-border` | Loading spinner | Submit button loading state |

---

## 🔑 Key Angular Concepts Used

### 1. Template-Driven Forms (FormsModule)

```html
<!-- Two-way data binding with ngModel -->
<input [(ngModel)]="formData.username" name="username" required minlength="3" #usernameField="ngModel" />

<!-- Conditional error message -->
@if (usernameField.invalid && usernameField.touched) {
  <div class="text-danger">Username must be at least 3 characters.</div>
}
```

| Concept | Syntax | Purpose |
|---------|--------|---------|
| Two-way binding | `[(ngModel)]="property"` | Input ↔ component property stay in sync |
| Template reference | `#usernameField="ngModel"` | Access the field's validation state in template |
| `touched` | `usernameField.touched` | True after user has focused and left the field |
| `invalid` | `usernameField.invalid` | True when validation rules fail |

### 2. Angular 17+ Control Flow

```html
<!-- Conditional rendering -->
@if (isLoading) {
  <span class="spinner-border"></span>
} @else {
  Register & Continue →
}

<!-- List rendering -->
@for (card of mockCards; track card.id) {
  <div class="card">{{ card.name }}</div>
}
```

> 💡 These replace the older `*ngIf` and `*ngFor` directives — cleaner syntax, better performance, and built into the framework (no imports needed).

### 3. Property Binding & Event Binding

| Syntax | Name | Direction | Example |
|--------|------|:---------:|---------|
| `{{ value }}` | Interpolation | Component → Template | `{{ card.cardNumber }}` |
| `[property]="value"` | Property binding | Component → Template | `[disabled]="isLoading"` |
| `(event)="handler()"` | Event binding | Template → Component | `(ngSubmit)="onLogin()"` |
| `[(ngModel)]="value"` | Two-way binding | Both directions | `[(ngModel)]="loginData.email"` |

### 4. `ChangeDetectorRef` — Forcing Re-Render with `withFetch()`

```typescript
private readonly cdr = inject(ChangeDetectorRef);

fetchCards(): void {
  this.cardService.getCardsByUserId(userId).subscribe({
    next: (cards) => {
      this.cards = cards;
      this.cdr.detectChanges();  // ← REQUIRED
    }
  });
}
```

> `provideHttpClient(withFetch())` uses the browser's native Fetch API, which resolves Promises **outside Angular's zone.js** change-detection zone. Without `cdr.detectChanges()`, the UI does not update even though the data has arrived.

### 5. `DestroyRef` + `takeUntilDestroyed` — Memory Leak Prevention

```typescript
private readonly destroyRef = inject(DestroyRef);

this.cardService.getCardsByUserId(userId)
  .pipe(takeUntilDestroyed(this.destroyRef))  // ← auto-unsubscribes when component is destroyed
  .subscribe({ next: (cards) => { ... } });
```

> Without `takeUntilDestroyed`, navigating away from the dashboard while an HTTP request is in flight could still trigger the subscribe callback and update a destroyed component's state.

### 6. `forkJoin` + `catchError` — Parallel Requests

```typescript
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

// Fire GET /api/transactions/card/{id} for every card simultaneously
const requests = this.cards.map(c =>
  this.cardService.getTransactionsByCardId(c.id)
    .pipe(catchError(() => of([])))  // ← if one card fails, return empty array
);

forkJoin(requests).subscribe({
  next: (results: TransactionResponse[][]) => {
    // results[0] = transactions for card[0], results[1] = for card[1], ...
    this.transactions = results.flat().sort(...);
  }
});
```

> `forkJoin` waits for **all** Observables to complete, then emits a single array of results. It's the RxJS equivalent of `Promise.all()`.

---

## 📚 Glossary

| Term | Definition |
|------|-----------|
| **SPA** | Single Page Application — loads once, updates dynamically without full page reloads |
| **Component** | A self-contained UI unit with its own template (HTML), styles (CSS), and logic (TypeScript) |
| **Standalone Component** | Angular 17+ pattern — components declare their own imports, no NgModule required |
| **Template** | The HTML file that defines a component's visual structure |
| **Router** | Angular's navigation system — maps URL paths to components |
| **router-outlet** | A placeholder in the template where the Router inserts the matched component |
| **Service** | A singleton class that provides data, HTTP calls, or shared logic to components |
| **Dependency Injection** | A design pattern where Angular automatically provides class dependencies via constructor |
| **Observable** | A stream of data that components can subscribe to — Angular's HttpClient returns Observables |
| **ngModel** | Angular directive for two-way data binding between form inputs and component properties |
| **Template-Driven Forms** | Angular forms approach using `ngModel` in the template — simpler than Reactive Forms |
| **Interpolation** | `{{ expression }}` — inserts a TypeScript expression's value into the HTML |
| **Property Binding** | `[attr]="value"` — binds a component property to an HTML element attribute |
| **Event Binding** | `(event)="handler()"` — calls a component method when an HTML event fires |
| **Bootstrap** | A CSS framework providing pre-built responsive components and utility classes |
| **TypeScript** | A typed superset of JavaScript — adds static types, interfaces, and compile-time error checking |
| **HttpClient** | Angular's built-in HTTP service for making REST API calls |

---

<p align="center">
  <b>🅰️ FinVault Frontend Documentation</b><br>
  <sub>Sprint 1 — SCRUM-15 (Angular Login & Dashboard UI) | Sprint 2 — SCRUM-16, SCRUM-17 | Hardening — SCRUM-18</sub><br>
  <sub>Part of the <a href="ARCHITECTURE.md">FinVault Documentation Suite</a></sub>
</p>

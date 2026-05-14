<p align="center">
  <img src="https://img.shields.io/badge/Angular-21-DD0031?style=for-the-badge&logo=angular&logoColor=white" />
  <img src="https://img.shields.io/badge/TypeScript-5.x-3178C6?style=for-the-badge&logo=typescript&logoColor=white" />
  <img src="https://img.shields.io/badge/Bootstrap-5-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white" />
  <img src="https://img.shields.io/badge/Standalone-Components-FF4081?style=for-the-badge" />
</p>

# 🅰️ FinVault — Frontend Documentation

> **Ticket:** SCRUM-15 | **Framework:** Angular 21 (Standalone Components)  
> **Styling:** Bootstrap 5 | **HTTP Client:** Angular `HttpClient` (provideHttpClient)

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
    │       "/dashboard" → DashboardComponent
    │       "/**" → redirect to "/login"
    │
    ├── 📄 app.config.ts                       ← ⚙️ Application-wide providers
    │   └── provideRouter(routes)
    │   └── provideHttpClient(withFetch())
    │
    ├── 📂 services/
    │   └── 📄 auth.service.ts                 ← 📡 HTTP communication with backend
    │       ├── register(payload) → POST /api/auth/register
    │       └── Interfaces: UserRegistrationRequest, RegistrationResponse
    │
    ├── 📂 login/
    │   ├── 📄 login.component.ts              ← 📝 Registration form logic
    │   │   ├── formData: { username, email, password }
    │   │   ├── isLoading, successMessage, errorMessage
    │   │   └── onSubmit() → calls AuthService.register()
    │   └── 📄 login.component.html            ← 🎨 Bootstrap card with form
    │       ├── Template-driven form with ngModel
    │       ├── Client-side validation (required, minlength, email)
    │       └── Success/error alerts, loading spinner
    │
    └── 📂 dashboard/
        ├── 📄 dashboard.component.ts          ← 📊 Mock card data (→ API in SCRUM-16)
        │   └── mockCards[]: 4 virtual cards with name, number, limit, status
        └── 📄 dashboard.component.html        ← 🎨 Full-page dashboard layout
            ├── Top navbar with brand and user avatar
            ├── Sidebar with navigation links
            ├── Summary stats row (4 stat cards)
            └── Virtual card grid using @for directive
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
  { path: 'dashboard', component: DashboardComponent },
  { path: '**',       redirectTo: 'login' }
];
```

### Route Breakdown

| Path | Component | Behavior | URL Example |
|:----:|:---------:|----------|:-----------:|
| `''` | — | Redirects to `/login` (home page) | `localhost:4200/` |
| `'login'` | `LoginComponent` | Shows registration form | `localhost:4200/login` |
| `'dashboard'` | `DashboardComponent` | Shows card management dashboard | `localhost:4200/dashboard` |
| `'**'` | — | Wildcard: catches all unknown URLs → redirects to `/login` | `localhost:4200/anything` |

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

### `AuthService` — HTTP Communication

```typescript
@Injectable({ providedIn: 'root' })    // ← Available app-wide (singleton)
export class AuthService {

  private readonly apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}  // ← HttpClient injected by Angular

  register(payload: UserRegistrationRequest): Observable<RegistrationResponse> {
    return this.http.post<RegistrationResponse>(
      `${this.apiUrl}/register`,
      payload
    );
    // Sends: POST http://localhost:8080/api/auth/register
    // Body:  { "username": "...", "email": "...", "password": "..." }
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

A centered Bootstrap card with a **user registration form** that communicates with the Spring Boot backend:

```
┌──────────────────────────────────────────┐
│              🌑 Dark Background           │
│                                           │
│    ┌──────────────────────────────────┐   │
│    │   💳 FinVault                    │   │  ← Blue header
│    │   Smart-Card Budgeting System    │   │
│    ├──────────────────────────────────┤   │
│    │                                  │   │
│    │   Create Your Account            │   │
│    │                                  │   │
│    │   ┌ ✅ Success Alert ──────────┐ │   │  ← Shows after registration
│    │   └────────────────────────────┘ │   │
│    │                                  │   │
│    │   Username:  [____________]      │   │  ← ngModel → formData.username
│    │   Email:     [____________]      │   │  ← ngModel → formData.email
│    │   Password:  [____________]      │   │  ← ngModel → formData.password
│    │                                  │   │
│    │   [ Register & Continue → ]      │   │  ← Calls onSubmit()
│    │                                  │   │
│    ├──────────────────────────────────┤   │
│    │   FinVault © 2026                │   │
│    └──────────────────────────────────┘   │
│                                           │
└──────────────────────────────────────────┘
```

### Component Logic — `login.component.ts`

```typescript
export class LoginComponent {

  // Two-way bound to form fields via ngModel
  formData: UserRegistrationRequest = {
    username: '',
    email: '',
    password: ''
  };

  isLoading = false;      // Shows spinner when true
  successMessage = '';     // Green alert text
  errorMessage = '';       // Red alert text

  constructor(
    private authService: AuthService,   // HTTP calls
    private router: Router              // Navigation
  ) {}

  onSubmit(): void {
    this.isLoading = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.authService.register(this.formData).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.successMessage = `${res.message} (ID: ${res.userId}). Redirecting...`;
        setTimeout(() => this.router.navigate(['/dashboard']), 1500);  // Auto-redirect
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.error || 'Registration failed. Please try again.';
      }
    });
  }
}
```

### Form Features

| Feature | Implementation | Purpose |
|---------|---------------|---------|
| **Two-way binding** | `[(ngModel)]="formData.username"` | Syncs input field ↔ TypeScript property in real-time |
| **Required validation** | `required` attribute + `#usernameField="ngModel"` | Prevents empty submissions |
| **Min-length** | `minlength="3"` on username, `minlength="6"` on password | Basic length validation |
| **Email format** | `type="email"` + `email` attribute | Angular validates email format |
| **Error messages** | `@if (usernameField.invalid && usernameField.touched)` | Shows only after user interacts with field |
| **Submit guard** | `[disabled]="loginForm.invalid \|\| isLoading"` | Prevents invalid/duplicate submissions |
| **Loading spinner** | `@if (isLoading)` → spinner inside button | Visual feedback during API call |
| **Auto-redirect** | `setTimeout(() => router.navigate(['/dashboard']), 1500)` | Navigates after showing success message |

---

## 📊 Component Deep Dive: DashboardComponent

### What It Does

A full-page Bootstrap dashboard layout with sidebar navigation and a virtual card grid:

```
┌──────────────────────────────────────────────────────────────────┐
│  💳 FinVault    Smart-Card Budgeting System       ● Online  [JD] │  ← Top Navbar
├────────┬─────────────────────────────────────────────────────────┤
│        │                                                         │
│  🏠    │  Welcome back, John Doe 👋                [+ New Card]  │
│  Dash  │  Here are your active virtual cards                     │
│        │                                                         │
│  💳    │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
│  Cards │  │ Total: 4 │ │Active: 3 │ │Frozen: 1 │ │₹11,500   │  │  ← Summary Stats
│        │  └──────────┘ └──────────┘ └──────────┘ └──────────┘  │
│  📊    │                                                         │
│  Trans │  MY VIRTUAL CARDS                                       │
│        │  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌─────┐ │
│  🔔    │  │ Groceries  │ │ Fuel       │ │ Entertain. │ │Utils│ │  ← Card Grid
│  Alerts│  │ **** 1234  │ │ **** 5678  │ │ **** 9012  │ │**** │ │
│        │  │ ₹5,000/day │ │ ₹2,000/day │ │ ₹1,500/day │ │₹3K  │ │
│  ⚙️    │  │ ACTIVE     │ │ ACTIVE     │ │ FROZEN     │ │ACT  │ │
│  Sett  │  │[Freeze][Det]│ │[Freeze][Det]│ │[Freeze][Det]│ │    │ │
│        │  └────────────┘ └────────────┘ └────────────┘ └─────┘ │
│  🚪    │                                                         │
│  Logout│                                                         │
├────────┴─────────────────────────────────────────────────────────┤
```

### Mock Data (Sprint 1)

```typescript
mockCards: MockCard[] = [
  { id: 1, name: 'Groceries',     number: '**** **** **** 1234', limit: '₹5,000 / day',  status: 'ACTIVE', color: 'primary' },
  { id: 2, name: 'Fuel',          number: '**** **** **** 5678', limit: '₹2,000 / day',  status: 'ACTIVE', color: 'success' },
  { id: 3, name: 'Entertainment', number: '**** **** **** 9012', limit: '₹1,500 / day',  status: 'FROZEN', color: 'warning' },
  { id: 4, name: 'Utilities',     number: '**** **** **** 3456', limit: '₹3,000 / day',  status: 'ACTIVE', color: 'info' },
];
```

> 📌 **Mock data will be replaced** with live API calls to `GET /api/cards/user/{userId}` in **SCRUM-16**.

### `@for` Directive — Rendering the Card Grid

```html
@for (card of mockCards; track card.id) {
  <div class="col-md-6 col-xl-3">
    <div class="card border-0 shadow text-white" [ngClass]="'bg-' + card.color">
      <div class="card-body p-4">
        <span class="badge">{{ card.status }}</span>
        <div class="font-monospace">{{ card.number }}</div>
        <div class="fw-bold">{{ card.name }}</div>
        <div class="small">Daily Limit: {{ card.limit }}</div>
      </div>
    </div>
  </div>
}
```

| Syntax | Purpose |
|--------|---------|
| `@for (card of mockCards; track card.id)` | Angular 17+ control flow — iterates over the array |
| `track card.id` | Tells Angular how to identify each item for efficient DOM updates |
| `[ngClass]="'bg-' + card.color"` | Dynamically assigns Bootstrap color class (e.g., `bg-primary`) |
| `{{ card.status }}` | Interpolation — inserts the value into the HTML |

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
| `{{ value }}` | Interpolation | Component → Template | `{{ card.name }}` |
| `[property]="value"` | Property binding | Component → Template | `[disabled]="isLoading"` |
| `(event)="handler()"` | Event binding | Template → Component | `(ngSubmit)="onSubmit()"` |
| `[(ngModel)]="value"` | Two-way binding | Both directions | `[(ngModel)]="formData.email"` |

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
  <sub>Sprint 1 — SCRUM-15 (Angular Login & Dashboard UI)</sub><br>
  <sub>Part of the <a href="ARCHITECTURE.md">FinVault Documentation Suite</a></sub>
</p>

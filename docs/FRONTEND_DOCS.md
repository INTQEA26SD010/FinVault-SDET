<p align="center">
  <img src="https://img.shields.io/badge/Angular-21.2-DD0031?style=for-the-badge&logo=angular&logoColor=white" />
  <img src="https://img.shields.io/badge/TypeScript-5.9-3178C6?style=for-the-badge&logo=typescript&logoColor=white" />
  <img src="https://img.shields.io/badge/Bootstrap-5.3-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white" />
  <img src="https://img.shields.io/badge/Standalone-Components-FF4081?style=for-the-badge" />
</p>

# 🅰️ FinVault — Frontend Documentation

> **Framework:** Angular 21 (Standalone Components) | **Styling:** Bootstrap 5.3  
> **HTTP Client:** `provideHttpClient(withFetch())` | **State:** sessionStorage

---

## 📑 Table of Contents

| # | Section |
|:-:|---------|
| 1 | [Angular Core Concepts](#-angular-core-concepts) |
| 2 | [Single Page Application (SPA)](#-single-page-application-spa) |
| 3 | [Standalone Components](#-standalone-components) |
| 4 | [Project Structure](#-project-structure) |
| 5 | [Application Bootstrap Flow](#-application-bootstrap-flow) |
| 6 | [Routing Configuration](#-routing-configuration) |
| 7 | [Route Guard (authGuard)](#-route-guard-authguard) |
| 8 | [Services & Dependency Injection](#-services--dependency-injection) |
| 9 | [Component: LoginComponent](#-component-logincomponent) |
| 10 | [Component: DashboardComponent](#-component-dashboardcomponent) |
| 11 | [Component: SimulatorComponent](#-component-simulatorcomponent) |
| 12 | [Change Detection with Fetch API](#-change-detection-with-fetch-api) |
| 13 | [Key Angular Patterns Used](#-key-angular-patterns-used) |

---

## 📖 Angular Core Concepts

### Angular — A Component-Based Framework

**Angular** is a TypeScript-based frontend framework by **Google** for building **Single Page Applications (SPAs)**.

| Concept | What It Is | FinVault Example |
|---------|-----------|------------------|
| **Component** | Self-contained UI unit (HTML + CSS + logic) | `LoginComponent`, `DashboardComponent`, `SimulatorComponent` |
| **Template** | HTML that defines the component's view | `login.component.html`, `dashboard.component.html` |
| **Service** | Class handling data, HTTP, or shared logic | `AuthService`, `VirtualCardService` |
| **Router** | Maps URLs to components without page reload | `app.routes.ts` |
| **Guard** | Protects routes from unauthorized access | `authGuard` |
| **Directive** | Instructions that modify the DOM | `@if`, `@for`, `ngModel` |

### 🎓 Why Angular for FinVault? (Interview)

> Angular is a **full framework** with built-in routing, HTTP client, forms, and DI — no need to piece together third-party libraries. Its TypeScript-first approach catches errors at compile time, and its opinionated structure enforces consistent code organization across the team.

---

## 🌐 Single Page Application (SPA)

```
  TRADITIONAL WEB APP                          SPA (Angular)
  ──────────────────                           ─────────────
  Click "Dashboard" →                          Click "Dashboard" →
  Browser requests NEW page                    Angular swaps component
  Server sends FULL HTML                       No server round-trip!
  White flash while loading                    Instant transition
  ⏱️ 500ms–2000ms                             ⏱️ ~10ms
```

### How FinVault's SPA Works

```
1. First visit → Browser downloads index.html + main.js (Angular bundle)
2. Angular boots up → renders <router-outlet /> in app.html
3. URL is "/login" → Router loads LoginComponent
4. User registers → Router navigates to "/dashboard"
5. DashboardComponent replaces LoginComponent — NO page reload
6. User clicks "Simulator" → Router loads SimulatorComponent — still no reload
```

---

## ⚡ Standalone Components

### The Modern Angular Pattern (17+)

FinVault uses **standalone components** — each component declares its own dependencies:

```typescript
// ❌ OLD: Required a shared AppModule
@NgModule({
  declarations: [LoginComponent, DashboardComponent],
  imports: [CommonModule, FormsModule, RouterModule],
})
export class AppModule {}

// ✅ NEW (FinVault): Each component is self-contained
@Component({
  selector: 'app-dashboard',
  standalone: true,                      // ← No NgModule needed
  imports: [CommonModule, FormsModule],  // ← Declares its own imports
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent {}
```

### 🎓 Why Standalone? (Interview)

| Benefit | Explanation |
|---------|-------------|
| **Tree-shakeable** | Unused components are excluded from the production build |
| **Self-documenting** | Each component's `imports` array shows exactly what it uses |
| **No module bloat** | No 200-line `AppModule` that imports everything |
| **Lazy-loadable** | Each component can be code-split independently |
| **Future direction** | Angular team recommends standalone for all new projects |

---

## 📂 Project Structure

```
frontend/src/
├── 📄 index.html                       ← SPA shell: <app-root></app-root>
├── 📄 main.ts                          ← bootstrapApplication(App, appConfig)
├── 📄 styles.css                       ← Global styles
│
└── 📂 app/
    ├── 📄 app.ts                       ← Root Component (just <router-outlet />)
    ├── 📄 app.html                     ← Template: <router-outlet />
    ├── 📄 app.css                      ← Minimal root styles
    ├── 📄 app.routes.ts                ← Route definitions (5 routes)
    ├── 📄 app.config.ts                ← provideRouter + provideHttpClient(withFetch())
    │
    ├── 📂 guards/
    │   └── 📄 auth.guard.ts            ← CanActivateFn: blocks /dashboard & /simulator
    │
    ├── 📂 services/
    │   ├── 📄 auth.service.ts          ← register(), login(), setSession(), logout()
    │   └── 📄 virtual-card.service.ts  ← CRUD cards + transactions
    │
    ├── 📂 login/
    │   ├── 📄 login.component.ts       ← Login + Signup tabbed logic
    │   └── 📄 login.component.html     ← Bootstrap card with two-tab form
    │
    ├── 📂 dashboard/
    │   ├── 📄 dashboard.component.ts   ← Cards grid, toggle, delete, simulate, transactions
    │   ├── 📄 dashboard.component.html ← 3-tab layout (Dashboard, My Cards, Transactions)
    │   └── 📄 dashboard.component.css  ← Dark navy theme, credit card chrome
    │
    └── 📂 simulator/
        ├── 📄 simulator.component.ts   ← QA testing tool: select card, enter amount, submit
        ├── 📄 simulator.component.html ← Form + SUCCESS/DECLINED alert display
        └── 📄 simulator.component.css  ← Dark theme, custom alerts
```

---

## 🚀 Application Bootstrap Flow

```
index.html
    │
    └── <app-root></app-root>
            │
            ▼
main.ts: bootstrapApplication(App, appConfig)
            │
            ├── appConfig provides:
            │   ├── provideRouter(routes)           ← enables <router-outlet>
            │   ├── provideHttpClient(withFetch())  ← enables HttpClient injection
            │   └── provideBrowserGlobalErrorListeners()
            │
            ▼
App Component renders:
    <router-outlet />
            │
            ▼
    Router evaluates URL → loads matching component
```

---

## 🛤️ Routing Configuration

```typescript
// app.routes.ts
export const routes: Routes = [
  { path: '',           redirectTo: 'login', pathMatch: 'full' },
  { path: 'login',      component: LoginComponent },
  { path: 'dashboard',  component: DashboardComponent,  canActivate: [authGuard] },
  { path: 'simulator',  component: SimulatorComponent,  canActivate: [authGuard] },
  { path: '**',         redirectTo: 'login' }
];
```

| Route | Component | Protected | Description |
|-------|-----------|:---------:|-------------|
| `/` | *(redirects)* | No | Redirects to `/login` |
| `/login` | LoginComponent | No | Login + signup form |
| `/dashboard` | DashboardComponent | ✅ authGuard | Card management dashboard |
| `/simulator` | SimulatorComponent | ✅ authGuard | QA transaction testing tool |
| `/**` | *(redirects)* | No | Wildcard → redirects unknown URLs to `/login` |

---

## 🔒 Route Guard (authGuard)

```typescript
// guards/auth.guard.ts
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }
  router.navigate(['/login']);
  return false;
};
```

### 🎓 Why `CanActivateFn` Instead of a Class? (Interview)

| Aspect | Class-Based Guard (old) | Functional Guard (FinVault) |
|--------|:-----------------------:|:---------------------------:|
| Syntax | `class AuthGuard implements CanActivate` | `const authGuard: CanActivateFn = () => {}` |
| DI | Constructor injection | `inject()` function |
| Boilerplate | requires `@Injectable()`, constructor | Just a function |
| Angular recommendation | Legacy | **Recommended since Angular 17** |

---

## 📡 Services & Dependency Injection

### AuthService

```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly SESSION_KEY = 'finvault_user';

  register(payload): Observable<RegistrationResponse>    // POST /api/auth/register
  login(payload): Observable<LoginResponse>              // POST /api/auth/login + auto setSession()
  setSession(user: SessionUser): void                    // sessionStorage.setItem(...)
  getSession(): SessionUser | null                       // sessionStorage.getItem(...)
  isLoggedIn(): boolean                                  // getSession() !== null
  logout(): void                                         // removeItem + navigate('/login')
}
```

### VirtualCardService

```typescript
@Injectable({ providedIn: 'root' })
export class VirtualCardService {
  getCardsByUserId(userId): Observable<VirtualCard[]>                    // GET /api/cards/user/{userId}
  createCard(userId, dailyLimit, vendorName): Observable<VirtualCard>    // POST /api/cards
  toggleCard(cardId): Observable<VirtualCard>                            // PUT /api/cards/{id}/toggle
  deleteCard(cardId): Observable<void>                                   // DELETE /api/cards/{id}
  processTransaction(cardId, amount, merchantName): Observable<TransactionResponse>  // POST /api/transactions
  getTransactionsByCardId(cardId): Observable<TransactionResponse[]>     // GET /api/transactions/card/{cardId}
}
```

### 🎓 Why `providedIn: 'root'`? (Interview)

> Services with `providedIn: 'root'` are **singleton** — one instance shared across the entire app. Angular's tree-shaker can also remove the service entirely if nothing injects it. This is the modern alternative to listing services in a module's `providers` array.

---

## 📝 Component: LoginComponent

| Property | Value |
|----------|-------|
| **Selector** | `app-login` |
| **Standalone** | Yes |
| **Imports** | `CommonModule`, `FormsModule` |
| **Route** | `/login` |

### Key Behavior

```
┌──────────────────────────────────────────────────┐
│  LoginComponent                                  │
│                                                  │
│  activeTab: 'login' | 'signup'                   │
│                                                  │
│  onLogin():                                      │
│    AuthService.login() → tap → setSession()      │
│    → router.navigate(['/dashboard'])             │
│                                                  │
│  onSignup():                                     │
│    AuthService.register() → setSession()         │
│    → router.navigate(['/dashboard'])             │
│                                                  │
│  If already logged in → auto-redirect to         │
│  /dashboard in constructor                       │
└──────────────────────────────────────────────────┘
```

---

## 📊 Component: DashboardComponent

| Property | Value |
|----------|-------|
| **Selector** | `app-dashboard` |
| **Standalone** | Yes |
| **Imports** | `CommonModule`, `FormsModule` |
| **Route** | `/dashboard` (guarded) |

### State Properties

| Property | Type | Purpose |
|----------|------|---------|
| `cards` | `VirtualCard[]` | All user's cards |
| `transactions` | `TransactionResponse[]` | Merged transaction history |
| `newCardLimit` | `number` | Form field: daily limit for new card |
| `newVendorName` | `string` | Form field: vendor name for new card |
| `activeNav` | `string` | Active sidebar tab (`'dashboard'`, `'cards'`, `'transactions'`) |
| `togglingCardId` | `number \| null` | Concurrency guard for freeze/unfreeze |
| `deletingCardId` | `number \| null` | Concurrency guard for card deletion |
| `processingCardId` | `number \| null` | Concurrency guard for quick-simulate |

### Key Methods

| Method | Trigger | Flow |
|--------|---------|------|
| `fetchCards()` | `ngOnInit`, after create/toggle/delete | GET → `cards = data` → `cdr.detectChanges()` |
| `generateCard()` | Form submit | POST (userId, limit, vendorName) → `fetchCards()` |
| `toggleCard(id)` | Freeze/Unfreeze button | PUT → `fetchCards()` |
| `deleteCard(id)` | Delete button | DELETE → `fetchCards()` |
| `simulatePurchase(id)` | Quick-buy button | POST (cardId, $50, "Coffee Shop") → `fetchCards()` |
| `fetchAllTransactions()` | Transactions tab activated | `forkJoin` per card → flatten → sort newest-first |
| `goToSimulator()` | Sidebar link | `router.navigate(['/simulator'])` |

### 🎓 Why Concurrency Guards? (Interview)

> `togglingCardId`, `deletingCardId`, and `processingCardId` prevent **concurrent HTTP calls** for the same card. If the user double-clicks "Freeze", only one PUT request fires. This prevents race conditions and provides visual feedback (button disabled while in-flight).

---

## 🧪 Component: SimulatorComponent

| Property | Value |
|----------|-------|
| **Selector** | `app-simulator` |
| **Standalone** | Yes |
| **Imports** | `CommonModule`, `FormsModule`, `RouterModule` |
| **Route** | `/simulator` (guarded) |

### Purpose

A dedicated **QA testing tool** that lets testers simulate transactions with full control over:
- Which card to charge (dropdown of ACTIVE cards only)
- The exact dollar amount
- The merchant name

### Key Behavior

```
1. ngOnInit → fetchCards() → filter ACTIVE cards into dropdown
2. User selects card, enters amount + merchant name
3. onSubmit() → POST /api/transactions
4. SUCCESS → green alert ("Transaction APPROVED — $X.XX charged to...")
5. DECLINED (200 with DECLINED status OR 422) → red alert ("exceeds daily limit")
```

### 🎓 Why a Dedicated `/simulator` Route? (Interview)

> The Dashboard has a quick-simulate button ($50 to "Coffee Shop"), but the Simulator provides **full control** for QA scenarios — testing edge cases like exact-limit amounts, boundary values, and multiple merchants. Separating it into its own route keeps the Dashboard focused on management, not testing.

---

## 🔄 Change Detection with Fetch API

### The Problem

`provideHttpClient(withFetch())` uses the native **Fetch API**, which resolves promises **outside Angular's zone.js**. HTTP callbacks don't automatically trigger template re-renders.

### The Solution Applied in FinVault

```typescript
private readonly cdr = inject(ChangeDetectorRef);

this.cardService.getCardsByUserId(userId).subscribe({
  next: (data) => {
    this.cards = data;
    this.cdr.detectChanges();  // ← Force Angular to re-render
  }
});
```

### 🎓 Why Use `withFetch()` Despite This Complexity? (Interview)

> The Fetch API is **lighter**, supports **streaming**, and aligns with modern web standards. XMLHttpRequest is legacy. The cost is one extra `detectChanges()` call per subscriber — a trivial trade-off for a modern HTTP stack.

---

## 🧩 Key Angular Patterns Used

| Pattern | Usage in FinVault | Why |
|---------|-------------------|-----|
| **Template-driven forms** | Login, signup, card creation | Simple two-way binding with `[(ngModel)]` — appropriate for small forms |
| **`@if` / `@for`** | Conditional rendering, card iteration | Modern control flow syntax (Angular 17+) |
| **`takeUntilDestroyed()`** | Dashboard subscriptions | Auto-unsubscribes when component is destroyed — prevents memory leaks |
| **`forkJoin()`** | Loading transactions for all cards in parallel | Waits for ALL requests to complete before emitting |
| **`catchError(() => of([]))`** | Transaction fetch fallback | If one card's request fails, others still render |
| **`tap()`** | Login response processing | Side effect (save session) without altering the stream |

---

<p align="center">
  <b>🅰️ FinVault Frontend Documentation</b><br>
  <sub>Angular 21 | 3 Components | 2 Services | 1 Route Guard | Standalone Architecture</sub><br>
  <sub>Part of the <a href="../README.md">FinVault Documentation Suite</a></sub>
</p>

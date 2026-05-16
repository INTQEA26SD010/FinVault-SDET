<p align="center">
  <img src="https://img.shields.io/badge/Angular-21.2-DD0031?style=for-the-badge&logo=angular&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Integration-REST%2FJSON-000000?style=for-the-badge&logo=json&logoColor=white" />
</p>

# 🔗 FinVault — Frontend ↔ Backend Integration Guide

> **Frontend:** Angular 21 on `http://localhost:4200`  
> **Backend:** Spring Boot 4 on `http://localhost:8080/api`  
> **Communication:** REST/JSON via Angular `HttpClient` (Fetch API)

---

## 📑 Table of Contents

| # | Section |
|:-:|---------|
| 1 | [Session Management](#-session-management) |
| 2 | [Route Protection](#-route-protection) |
| 3 | [Service: AuthService](#-service-authservice) |
| 4 | [Service: VirtualCardService](#-service-virtualcardservice) |
| 5 | [Data Flow: Dashboard Lifecycle](#-data-flow-dashboard-lifecycle) |
| 6 | [Data Flow: Simulator Lifecycle](#-data-flow-simulator-lifecycle) |
| 7 | [Change Detection Strategy](#-change-detection-strategy) |
| 8 | [TypeScript Interfaces](#-typescript-interfaces) |
| 9 | [Error Handling Patterns](#-error-handling-patterns) |
| 10 | [Running the Full Stack](#-running-the-full-stack) |

---

## 🔐 Session Management

After successful login or signup, the frontend stores the user session in `sessionStorage`:

```json
// Key: "finvault_user"
{ "userId": 1, "username": "johndoe", "email": "john@example.com" }
```

| Method | Purpose |
|--------|---------|
| `authService.setSession(user)` | Write to `sessionStorage` |
| `authService.getSession()` | Read → returns `SessionUser \| null` |
| `authService.isLoggedIn()` | Returns `true` if session key exists |
| `authService.logout()` | Clears `sessionStorage` + navigates to `/login` |

### 🎓 Why sessionStorage Over localStorage? (Interview)

> `sessionStorage` **auto-clears when the browser tab closes** — appropriate for a financial application where sessions should not persist indefinitely. `localStorage` would keep the user "logged in" even after closing the browser, which is a security risk without proper token expiration.

---

## 🛡️ Route Protection

The `/dashboard` and `/simulator` routes are protected by `authGuard`:

```typescript
// app.routes.ts
{ path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] }
{ path: 'simulator', component: SimulatorComponent, canActivate: [authGuard] }
```

**Logic:** `authGuard` calls `authService.isLoggedIn()` → if `false`, redirects to `/login`.

---

## 📡 Service: AuthService

| Method | HTTP | Endpoint | Notes |
|--------|:----:|----------|-------|
| `register(payload)` | `POST` | `/api/auth/register` | Returns `{ message, userId }` |
| `login(payload)` | `POST` | `/api/auth/login` | Returns `{ userId, username, email, message }`; auto-calls `setSession()` via `tap()` |

### Login Data Flow

```
User enters email + password
  → onLogin()
    → authService.login({ email, password })
      → POST http://localhost:8080/api/auth/login
        → 200 OK: { userId, username, email, message }
      → tap(): sessionStorage.setItem('finvault_user', JSON.stringify({userId, username, email}))
    → router.navigate(['/dashboard'])
```

### Signup Data Flow

```
User enters username + email + password
  → onSignup()
    → authService.register({ username, email, password })
      → POST http://localhost:8080/api/auth/register
        → 201 Created: { message, userId }
    → authService.setSession({ userId, username, email })
    → router.navigate(['/dashboard'])
```

---

## 💳 Service: VirtualCardService

| Method | HTTP | Endpoint | Purpose |
|--------|:----:|----------|---------|
| `getCardsByUserId(userId)` | `GET` | `/api/cards/user/{userId}` | Fetch all virtual cards for a user |
| `createCard(userId, dailyLimit, vendorName)` | `POST` | `/api/cards` | Generate a new card with vendor label |
| `toggleCard(cardId)` | `PUT` | `/api/cards/{id}/toggle` | Freeze/unfreeze; returns updated card |
| `deleteCard(cardId)` | `DELETE` | `/api/cards/{id}` | Permanently delete card + transactions |
| `processTransaction(cardId, amount, merchantName)` | `POST` | `/api/transactions` | Simulate; returns SUCCESS (200) or DECLINED (422) |
| `getTransactionsByCardId(cardId)` | `GET` | `/api/transactions/card/{cardId}` | Fetch history, newest-first |

---

## 📊 Data Flow: Dashboard Lifecycle

### 1. Initialization (`ngOnInit`)

```
getSession() → user = { userId, username, email }
fetchCards() → GET /api/cards/user/{userId}
  → cards = response
  → cdr.detectChanges()
```

### 2. Create Card

```
Form submit (validated: limit > 0, vendorName not empty)
  → generateCard()
    → POST /api/cards { userId, dailyLimit, vendorName }
    → next: fetchCards()  ← refresh the grid
    → cdr.detectChanges()
```

### 3. Toggle Card (Freeze / Unfreeze)

```
Button click → toggleCard(cardId)
  → togglingCardId = cardId          (disables button while in-flight)
  → PUT /api/cards/{id}/toggle
  → next: togglingCardId = null; fetchCards()
  → cdr.detectChanges()
```

### 4. Delete Card

```
Button click → deleteCard(cardId)
  → deletingCardId = cardId           (disables button while in-flight)
  → DELETE /api/cards/{id}
  → next: deletingCardId = null; fetchCards()
  → cdr.detectChanges()
```

### 5. Quick Simulate Purchase ($50 Coffee Shop)

```
Button click → simulatePurchase(cardId, event)
  → event.stopPropagation()          (prevents ancestor handlers)
  → processingCardId = cardId
  → POST /api/transactions { cardId, amount: 50, merchantName: "Coffee Shop" }
  → next: fetchCards() (refresh balances)
  → error 422: show "daily limit reached" message
  → cdr.detectChanges()
```

### 6. Transactions Tab

```
setActiveNav('transactions') → fetchAllTransactions()
  → forkJoin(cards.map(c => getTransactionsByCardId(c.id)))
  → catchError(() => of([])) per card  ← graceful fallback
  → flat() → sort by timestamp DESC
  → transactions = result
  → cdr.detectChanges()
```

---

## 🧪 Data Flow: Simulator Lifecycle

### Initialization

```
ngOnInit()
  → user = getSession()
  → if (!user) → router.navigate(['/login'])
  → fetchCards()
    → cards = all cards
    → activeCards = cards.filter(c => c.status === 'ACTIVE')
    → selectedCardId = activeCards[0].id  (auto-select first)
```

### Transaction Submission

```
onSubmit() (validated: card selected, amount > 0, merchant not empty)
  → isSubmitting = true
  → POST /api/transactions { cardId: selectedCardId, amount, merchantName }
  → next:
      if status === 'SUCCESS' → green alert ("APPROVED — $X.XX charged...")
      if status === 'DECLINED' → red alert ("exceeds daily limit")
  → error (422):
      → red alert with DECLINED message
  → cdr.detectChanges()
```

---

## 🔄 Change Detection Strategy

### Why Manual `detectChanges()`?

`provideHttpClient(withFetch())` uses the **native Fetch API**, which resolves outside Angular's zone.js. HTTP callbacks don't automatically trigger re-renders.

### Pattern Applied

```typescript
private readonly cdr = inject(ChangeDetectorRef);

someService.getData().subscribe({
  next: (data) => {
    this.data = data;
    this.cdr.detectChanges();  // ← Required for template updates
  },
  error: (err) => {
    this.errorMsg = 'Failed';
    this.cdr.detectChanges();  // ← Also required in error paths
  }
});
```

### 🎓 Interview Explanation

> "We chose `withFetch()` because it's the modern HTTP transport, lighter than XMLHttpRequest, and supports streaming. The trade-off is calling `detectChanges()` after each async callback. In production, OnPush change detection + signals would eliminate this entirely."

---

## 📋 TypeScript Interfaces

```typescript
// ── Cards ────────────────────────────────────────────────────

export interface VirtualCard {
  id: number;
  cardNumber: string;
  cvv: string;
  dailyLimit: number;
  balance: number;
  status: string;        // 'ACTIVE' | 'FROZEN'
  vendorName: string;    // Vendor/purpose label (e.g., "Amazon", "Netflix")
  userId: number;
}

// ── Transactions ─────────────────────────────────────────────

export interface TransactionResponse {
  id: number;
  cardId: number;
  amount: number;
  merchantName: string;
  timestamp: string;     // ISO-8601 from backend LocalDateTime
  status: string;        // 'SUCCESS' | 'DECLINED'
}

// ── Auth ─────────────────────────────────────────────────────

export interface SessionUser {
  userId: number;
  username: string;
  email: string;
}

export interface UserRegistrationRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  userId: number;
  username: string;
  email: string;
  message: string;
}

export interface RegistrationResponse {
  message: string;
  userId: number;
}
```

---

## ⚠️ Error Handling Patterns

| Scenario | HTTP Status | Frontend Handling |
|----------|:-----------:|-------------------|
| Backend unreachable | Network error | `error: () => errorMsg = "Is the backend running?"` |
| Email already taken | `400` | Display `err.error.error` message |
| Invalid login | `401` | Display "Invalid email or password" |
| Transaction declined | `422` | Show DECLINED alert (body has `status: 'DECLINED'`) |
| Card not found (toggle/delete) | `404` | Generic error message |
| Create card — user not found | `500` | Generic error message |

### The 422 Pattern (Transaction Declined)

The backend returns **422 Unprocessable Entity** when a transaction is declined, with the full transaction DTO in the body:

```typescript
// Dashboard handles 422 in the error callback:
error: (err: HttpErrorResponse) => {
  if (err.status === 422 && err.error?.status === 'DECLINED') {
    this.errorMsg = 'Transaction declined — daily limit reached.';
  }
}

// Simulator handles it similarly but with richer messages:
error: (err) => {
  if (err?.status === 422 && err.error?.status === 'DECLINED') {
    this.alertType = 'danger';
    this.alertMessage = `DECLINED — exceeds daily limit.`;
  }
}
```

---

## 🚀 Running the Full Stack

```powershell
# Terminal 1: Backend (Spring Boot)
cd backend
.\mvnw.cmd spring-boot:run
# → http://localhost:8080

# Terminal 2: Frontend (Angular)
cd frontend
npm install          # first time only
npx ng serve
# → http://localhost:4200
```

> Angular dev server runs on `http://localhost:4200` and hits `http://localhost:8080` directly (no proxy file — CORS is configured on the backend).

---

<p align="center">
  <b>🔗 FinVault Frontend ↔ Backend Integration Guide</b><br>
  <sub>2 services | 6 HTTP methods | sessionStorage sessions | ChangeDetectorRef pattern</sub><br>
  <sub>Part of the <a href="../README.md">FinVault Documentation Suite</a></sub>
</p>

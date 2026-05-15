# FinVault — Frontend Integration Guide

> **Tickets:** SCRUM-15, SCRUM-16, SCRUM-17, SCRUM-18  
> **Backend base URL:** `http://localhost:8080/api`

---

## Auth Session Management

After a successful login or signup, the frontend stores the user session in `sessionStorage` under the key `finvault_user`:

```json
{ "userId": 1, "username": "johndoe", "email": "john@example.com" }
```

| Method | Purpose |
|---|---|
| `authService.setSession(user)` | Write session to `sessionStorage` |
| `authService.getSession()` | Read session — returns `SessionUser \| null` |
| `authService.isLoggedIn()` | Returns `true` if session key exists |
| `authService.logout()` | Clears `sessionStorage` and navigates to `/login` |

---

## Route Guard: `AuthGuard`

The `/dashboard` route is protected by `authGuard`:

```typescript
// app.routes.ts
{ path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] }
```

`authGuard` calls `authService.isLoggedIn()`. If `false`, redirects to `/login`. This means signup/login flows **must call `authService.setSession()` before navigating to `/dashboard`**.

---

## Service: `AuthService`

| Method | HTTP | Endpoint | Notes |
|---|---|---|---|
| `register(payload)` | `POST` | `/api/auth/register` | Returns `{ message, userId }` |
| `login(payload)` | `POST` | `/api/auth/login` | Returns `{ userId, username, email, message }`; auto-calls `setSession()` in `tap()` |

---

## Service: `VirtualCardService`

| Method | HTTP | Endpoint | Purpose |
|---|---|---|---|
| `getCardsByUserId(userId)` | `GET` | `/api/cards/user/{userId}` | Fetch all virtual cards for a user |
| `createCard(userId, dailyLimit)` | `POST` | `/api/cards` | Generate a new virtual card |
| `processTransaction(cardId, amount, merchantName)` | `POST` | `/api/transactions` | Simulate a spend; returns `SUCCESS (200)` or `DECLINED (422)` |
| `getTransactionsByCardId(cardId)` | `GET` | `/api/transactions/card/{cardId}` | Fetch all transactions for a card, newest-first |

---

## Data Flow: Dashboard Lifecycle

### 1. Init (`ngOnInit`)
```
getSession() → fetchCards() → getCardsByUserId(session.userId)
```

### 2. Create Card
```
form submit → generateCard() → POST /api/cards → fetchCards()
```

### 3. Simulate Purchase
```
button click (type="button") → simulatePurchase(cardId, event)
    → event.stopPropagation()          (blocks click bubbling)
    → processingCardId = cardId        (disables all buttons)
    → POST /api/transactions
    → next: fetchCards()               (refresh balances)
    → error 422: treat as DECLINED     (show error message, still refresh)
    → processingCardId = null          (re-enable buttons)
```

### 4. Transactions Tab
```
setActiveNav('transactions') → fetchAllTransactions()
    → forkJoin(cards.map(c => getTransactionsByCardId(c.id)))
    → flatten + sort by timestamp DESC
    → cdr.detectChanges()             (force render — required for withFetch())
```

---

## Change Detection Note

`provideHttpClient(withFetch())` uses the native Fetch API, which resolves promises **outside Angular's zone.js**. This means HTTP callbacks do not automatically trigger template re-renders.

**Fix applied:** `ChangeDetectorRef.detectChanges()` is called at the end of every `subscribe` `next` and `error` callback in `DashboardComponent`.

---

## TypeScript Interfaces

```typescript
// virtual-card.service.ts
export interface VirtualCard {
  id: number;
  cardNumber: string;
  cvv: string;
  dailyLimit: number;
  balance: number;
  userId: number;
}

export interface TransactionResponse {
  id: number;
  cardId: number;
  amount: number;
  merchantName: string;
  timestamp: string;     // ISO-8601 string from backend LocalDateTime
  status: string;        // 'SUCCESS' | 'DECLINED'
}
```

---

## Running

```powershell
# Backend (Spring Boot)
cd backend
.\mvnw.cmd spring-boot:run

# Frontend (Angular) — must cd into frontend first
cd frontend
npx ng serve
```

Angular dev server runs on `http://localhost:4200` and hits `http://localhost:8080` directly (no proxy).

# FinVault — Frontend Integration Guide

> **Tickets:** SCRUM-15, SCRUM-16, SCRUM-17, SCRUM-18, SCRUM-19, SCRUM-20, SCRUM-21  
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

The `/dashboard` and `/simulator` routes are protected by `authGuard`:

```typescript
// app.routes.ts
{ path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] }
{ path: 'simulator', component: SimulatorComponent, canActivate: [authGuard] }
```

`authGuard` calls `authService.isLoggedIn()`. If `false`, redirects to `/login`.

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
| `createCard(userId, dailyLimit, vendorName)` | `POST` | `/api/cards` | Generate a new virtual card with vendor label |
| `toggleCard(cardId)` | `PUT` | `/api/cards/{id}/toggle` | Freeze or unfreeze a card; returns updated card |
| `deleteCard(cardId)` | `DELETE` | `/api/cards/{id}` | Permanently delete a card and all its transactions |
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

### 3. Toggle Card (Freeze / Unfreeze)
```
Freeze/Unfreeze button click → toggleCard(cardId)
    → togglingCardId = cardId          (disables button while in-flight)
    → PUT /api/cards/{id}/toggle
    → next: update cards[idx] in-place  (status flips ACTIVE ↔ FROZEN)
    → cdr.detectChanges()
    → togglingCardId = null             (re-enable button)
```

### 4. Delete Card
```
Delete button click → deleteCard(cardId)
    → deletingCardId = cardId           (disables button while in-flight)
    → DELETE /api/cards/{id}
    → next: filter card out of cards[]  (removes from UI immediately)
    → cdr.detectChanges()
    → deletingCardId = null
```

### 5. Simulate Purchase
```
button click → processTransaction(cardId, amount, merchantName)
    → POST /api/transactions
    → next: fetchCards()               (refresh balances)
    → error 422: treat as DECLINED
```

### 6. Transactions Tab
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
  status: string;        // 'ACTIVE' | 'FROZEN'
  vendorName: string;    // Vendor or purpose label (e.g. "Amazon")
  userId?: number;
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

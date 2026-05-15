# FinVault — Frontend Integration Guide (SCRUM-18)

## Overview

The Angular frontend communicates with the Spring Boot backend (`http://localhost:8080`) via `HttpClient`. All HTTP calls are centralised in **`VirtualCardService`**.

## Service: `VirtualCardService`

| Method | HTTP | Endpoint | Purpose |
|---|---|---|---|
| `getCardsByUserId(userId)` | `GET` | `/api/cards/user/{userId}` | Fetch all virtual cards for a user |
| `createCard(userId, dailyLimit)` | `POST` | `/api/cards` | Generate a new virtual card |
| `processTransaction(cardId, amount, merchantName)` | `POST` | `/api/transactions` | Deduct balance from a card |

## Data Flow

1. **Dashboard Init** — `ngOnInit` calls `getCardsByUserId(1)` (hardcoded user) and populates the card grid.
2. **Create Card** — The "Generate New Card" form posts `{ userId, dailyLimit }` and refreshes the card list on success.
3. **Simulate Purchase** — Each card's "Simulate Purchase" button sends a `$50 / Coffee` transaction, then refreshes cards to reflect the updated balance.

## Configuration

`HttpClient` is provided via `provideHttpClient(withFetch())` in `app.config.ts`.  
No interceptors or auth tokens are configured yet — those will be added when the login session manager is implemented.

## Running

```bash
# Backend (Spring Boot)
cd backend && ./mvnw spring-boot:run

# Frontend (Angular)
cd frontend && ng serve
```

The Angular dev server proxies nothing — it hits `localhost:8080` directly via absolute URLs.

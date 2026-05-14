# FinVault — Frontend Documentation

> **Ticket:** SCRUM-15 | **Framework:** Angular 21 (Standalone Components)  
> **Styling:** Bootstrap 5 | **HTTP Client:** Angular `HttpClient` (provideHttpClient)

---

## Overview

The FinVault frontend is a **Single Page Application (SPA)** built with Angular 21 using the modern standalone component architecture — no `NgModule` required. Bootstrap 5 is registered globally in `angular.json` and available to every component without per-file imports.

---

## Component Structure

```
frontend/src/app/
│
├── app.ts                          ← Root component (shell — just <router-outlet />)
├── app.html                        ← <router-outlet /> only
├── app.routes.ts                   ← Centralized route definitions
├── app.config.ts                   ← App-level providers (Router, HttpClient)
│
├── services/
│   └── auth.service.ts             ← HTTP calls to /api/auth (register)
│
├── login/
│   ├── login.component.ts          ← Registration form logic + AuthService call
│   └── login.component.html        ← Bootstrap-styled register/login card
│
└── dashboard/
    ├── dashboard.component.ts      ← Mock card data (replaced with API in SCRUM-16)
    └── dashboard.component.html    ← Sidebar layout + virtual card grid
```

---

## Routing Configuration

**File:** `src/app/app.routes.ts`

| Path | Component | Behaviour |
|---|---|---|
| `/` | — | Redirects to `/login` |
| `/login` | `LoginComponent` | User registration form |
| `/dashboard` | `DashboardComponent` | Card management dashboard |
| `/**` | — | Wildcard — redirects to `/login` |

After a successful registration, the `LoginComponent` automatically navigates to `/dashboard` after a 1.5 second delay showing the success message.

---

## Services

### `AuthService` — `src/app/services/auth.service.ts`

Handles all HTTP communication with the Spring Boot backend.

| Method | HTTP | Endpoint | Purpose |
|---|---|---|---|
| `register(payload)` | `POST` | `/api/auth/register` | Creates a new FinVault user account |

`HttpClient` is provided application-wide via `provideHttpClient(withFetch())` in `app.config.ts` — no separate `HttpClientModule` import needed (Angular 17+ standalone pattern).

---

## Components

### `LoginComponent` — `/login`

A centered Bootstrap card with:
- Template-driven form (`FormsModule`) with Angular `ngModel` two-way binding
- Client-side validation: required fields, min-length, email format
- Real-time error messages shown on field `touched`
- Loading spinner on submit button while the API call is in progress
- Bootstrap alert banners for success / error API responses
- Auto-redirects to `/dashboard` on successful registration

### `DashboardComponent` — `/dashboard`

A full-page Bootstrap layout with:
- **Top navbar** — FinVault brand, status badge, user avatar
- **Sidebar** — Navigation links (Dashboard, Cards, Transactions, Alerts, Settings, Logout)
- **Summary stats row** — 4 stat cards (Total Cards, Active, Frozen, Total Daily Limit)
- **Virtual card grid** — 4 mock Bootstrap cards using `@for` directive, colour-coded by type
- Cards show card number (masked), category name, daily limit, and status badge
- Mock data is hardcoded for Sprint 1 — will be replaced with `VirtualCardService` HTTP calls in **SCRUM-16**

---

## HttpClient Setup

**File:** `src/app/app.config.ts`

```typescript
provideHttpClient(withFetch())
```

`withFetch()` uses the browser's native `fetch` API instead of `XMLHttpRequest`, aligning with Angular 21's recommended pattern and enabling better integration with Angular's SSR (Server-Side Rendering) if adopted later.

---

*Last updated: Sprint 1 — SCRUM-15 (Angular Login & Dashboard UI)*

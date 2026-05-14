# FinVault — REST API Documentation

> **Ticket:** SCRUM-14 | **Base URL (local):** `http://localhost:8080`  
> **Format:** JSON | **Auth:** None (Sprint 1 — open endpoints; JWT added in security sprint)

---

## Overview

This document describes the first two REST API endpoints delivered in Sprint 1. These endpoints form the entry point of the FinVault backend: user registration and card retrieval. The backend follows a strict **3-layer architecture**: Controller → Service → Repository, ensuring business logic never leaks into the HTTP layer.

---

## Package Structure

```
com.finvault.backend
├── config/
│   └── SecurityConfig.java         ← BCryptPasswordEncoder bean + open permit-all filter chain
├── dto/
│   ├── UserRegistrationDto.java     ← Inbound: registration request body
│   └── VirtualCardResponseDto.java  ← Outbound: card data (CVV excluded)
├── service/
│   ├── UserService.java             ← Registration logic + BCrypt hashing
│   └── VirtualCardService.java      ← Card fetching + entity→DTO mapping
└── controller/
    ├── AuthController.java          ← POST /api/auth/register
    └── VirtualCardController.java   ← GET  /api/cards/user/{userId}
```

---

## Endpoint 1: Register User

### `POST /api/auth/register`

Creates a new FinVault user account. The raw password is hashed using **BCrypt (strength 10)** before being stored — plaintext passwords are never persisted.

**Request Body** (`application/json`)

```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

**Success Response** — `201 Created`

```json
{
  "message": "User registered successfully",
  "userId": 1
}
```

**Error Response** — `400 Bad Request` (email already registered)

```json
{
  "error": "Email is already registered: john@example.com"
}
```

| Field | Validation |
|---|---|
| `username` | Required, stored as-is |
| `email` | Required, must be unique in `users` table |
| `password` | Required; hashed by BCrypt before storage; plaintext discarded |

---

## Endpoint 2: Get User's Virtual Cards

### `GET /api/cards/user/{userId}`

Returns all virtual cards owned by the specified user. The **CVV** and raw **user FK** are deliberately excluded from the response DTO — only display-safe fields are exposed.

**Path Parameter**

| Parameter | Type | Description |
|---|---|---|
| `userId` | `Long` | The database ID of the target user |

**Success Response** — `200 OK`

```json
[
  {
    "id": 1,
    "cardNumber": "4111111111111111",
    "dailyLimit": 500.00,
    "status": "ACTIVE"
  },
  {
    "id": 2,
    "cardNumber": "4222222222222222",
    "dailyLimit": 200.00,
    "status": "FROZEN"
  }
]
```

Returns `[]` (empty array) if the user has no cards — never returns `null`.

---

## Architecture: Why DTOs?

Returning JPA `@Entity` objects directly from a controller is an anti-pattern because:

1. **Over-exposure** — Entity fields like `passwordHash` and `cvv` would be serialised into the JSON response.
2. **Tight coupling** — A database column rename would break the API contract.
3. **Circular serialisation** — Bidirectional JPA relationships (`User ↔ VirtualCard`) cause `StackOverflowError` during JSON serialisation.

DTOs act as an explicit, controlled API contract that is independent of the database schema.

---

## Security Configuration (Sprint 1 Baseline)

Spring Security was added to the classpath in this sprint to provide `BCryptPasswordEncoder`. A `SecurityConfig` class was created with:

- **All endpoints open** (`anyRequest().permitAll()`) — temporary for Sprint 1 development.
- **CSRF disabled** — correct for stateless REST APIs that don't use session cookies.
- **JWT filter placeholder** — will be inserted in the dedicated security sprint.

---

*Last updated: Sprint 1 — SCRUM-14 (REST APIs for User Registration and Card Fetching)*

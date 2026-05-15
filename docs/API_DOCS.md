<p align="center">
  <img src="https://img.shields.io/badge/REST%20API-Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Format-JSON-000000?style=for-the-badge&logo=json&logoColor=white" />
  <img src="https://img.shields.io/badge/Auth-JWT%20(Planned)-FF6B6B?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Port-8080-blue?style=for-the-badge" />
</p>

# 📡 FinVault — REST API Documentation

> **Ticket:** SCRUM-14 | **Base URL (local):** `http://localhost:8080`  
> **Format:** JSON | **Auth:** None (Sprint 1 — open endpoints; JWT added in security sprint)

---

## 📑 Table of Contents

| # | Section | Description |
|:-:|---------|-------------|
| 1 | [What is a REST API? — Theory](#-what-is-a-rest-api--theory) | Foundational web API concepts |
| 2 | [HTTP Methods Explained](#-http-methods-explained) | GET, POST, PUT, DELETE and when to use each |
| 3 | [HTTP Status Codes](#-http-status-codes) | Understanding response codes |
| 4 | [FinVault API Overview](#-finvault-api-overview) | Quick reference of all endpoints |
| 5 | [The 3-Layer Request Flow](#-the-3-layer-request-flow) | How a request travels through the backend |
| 6 | [Endpoint 1: Register User](#-endpoint-1-register-user) | `POST /api/auth/register` |
| 7 | [Endpoint 2: Login User](#-endpoint-2-login-user) | `POST /api/auth/login` |
| 8 | [Endpoint 3: Get User's Virtual Cards](#-endpoint-3-get-users-virtual-cards) | `GET /api/cards/user/{userId}` |
| 9 | [Endpoint 4: Create Virtual Card](#-endpoint-4-create-virtual-card) | `POST /api/cards` |
| 10 | [Endpoint 5: Simulate Transaction](#-endpoint-5-simulate-transaction) | `POST /api/transactions` |
| 11 | [Endpoint 6: Get Transactions by Card](#-endpoint-6-get-transactions-by-card) | `GET /api/transactions/card/{cardId}` |
| 12 | [Why DTOs? — Anti-Pattern Prevention](#-why-dtos--anti-pattern-prevention) | Why we never return @Entity directly |
| 13 | [Security Configuration](#-security-configuration) | BCrypt + permitAll baseline |
| 14 | [Testing the API with cURL](#-testing-the-api-with-curl) | Ready-to-use commands |
| 15 | [Glossary](#-glossary) | Key API terms |

---

## 📖 What is a REST API? — Theory

### REST — Representational State Transfer

**REST** is an architectural style for designing web services. It was introduced by Roy Fielding in his 2000 PhD dissertation and has become the **de facto standard** for web APIs.

> 💡 **Think of a REST API as a waiter in a restaurant:**  
> You (the client/Angular) give the waiter (the API) your order (HTTP request).  
> The waiter takes it to the kitchen (server/database), and brings back your food (JSON response).  
> You never go into the kitchen yourself — the waiter is your **interface**.

### The 6 REST Principles

| Principle | Meaning | FinVault Implementation |
|:---------:|---------|-------------------------|
| **1. Client-Server** | Frontend and backend are separate | Angular (4200) ↔ Spring Boot (8080) |
| **2. Stateless** | Each request contains ALL info needed — no server-side sessions | No HTTP sessions; JWT will carry auth state |
| **3. Cacheable** | Responses can be cached for performance | HTTP headers (future implementation) |
| **4. Uniform Interface** | Consistent URL patterns and HTTP methods | `/api/auth/register`, `/api/cards/user/{id}` |
| **5. Layered System** | Client doesn't know if it's talking to the actual server or a proxy | CORS-ready; load balancer compatible |
| **6. Code on Demand** (optional) | Server can send executable code to client | Not used in FinVault |

### What is JSON?

**JSON** (JavaScript Object Notation) is the data format used by REST APIs to exchange data:

```json
{
  "username": "johndoe",        ← key-value pair (string)
  "age": 25,                    ← number
  "premium": true,              ← boolean
  "cards": [                    ← array of objects
    { "id": 1, "status": "ACTIVE" },
    { "id": 2, "status": "FROZEN" }
  ]
}
```

---

## 🔤 HTTP Methods Explained

HTTP methods tell the server **what action** to perform on a resource:

| Method | CRUD Action | Purpose | Example | FinVault Usage |
|:------:|:-----------:|---------|---------|:--------------:|
| `GET` | **R**ead | Retrieve data without modifying anything | Get all user's cards | ✅ `GET /api/cards/user/{userId}` |
| `POST` | **C**reate | Submit new data to be created/processed | Register a new user | ✅ `POST /api/auth/register` |
| `PUT` | **U**pdate | Replace an existing resource entirely | Update card daily limit | 🔜 Future sprint |
| `PATCH` | **U**pdate | Partially modify an existing resource | Freeze a single card | 🔜 Future sprint |
| `DELETE` | **D**elete | Remove a resource | Cancel a virtual card | 🔜 Future sprint |

> 💡 **GET is safe & idempotent** — calling it 100 times has the same effect as calling it once. It never modifies data.  
> 💡 **POST is NOT idempotent** — calling it 100 times creates 100 users!

---

## 📊 HTTP Status Codes

Every API response includes a **status code** — a 3-digit number that describes the outcome:

### Status Code Families

| Range | Family | Meaning | Icon |
|:-----:|--------|---------|:----:|
| **1xx** | Informational | Request received, continuing... | ℹ️ |
| **2xx** | Success | Request was successful | ✅ |
| **3xx** | Redirection | Further action needed | ↪️ |
| **4xx** | Client Error | Problem with the request (your fault) | ❌ |
| **5xx** | Server Error | Problem on the server (our fault) | 💥 |

### Codes Used in FinVault

| Code | Name | When FinVault Returns It |
|:----:|------|--------------------------|
| `200` | OK | Cards retrieved, login success, transaction response |
| `201` | Created | New user registered (`POST /api/auth/register`), new card created (`POST /api/cards`) |
| `400` | Bad Request | Email already registered; invalid credentials on login |
| `401` | Unauthorized | Invalid/missing JWT token (future sprint) |
| `404` | Not Found | User/card not found |
| `422` | Unprocessable Entity | Transaction declined — daily limit would be exceeded |
| `500` | Internal Server Error | Unhandled server exception (should never happen in production) |

---

## 🗺️ FinVault API Overview

### Quick Reference — All Endpoints

| Method | Endpoint | Purpose | Request Body | Response |
|:------:|----------|---------|:------------:|----------|
| `POST` | `/api/auth/register` | Register new user | ✅ JSON | `201` + userId |
| `POST` | `/api/auth/login` | Login existing user | ✅ JSON | `200` + userId, username, email |
| `GET` | `/api/cards/user/{userId}` | Get user's cards | ❌ None | `200` + card list |
| `POST` | `/api/cards` | Create a new virtual card | ✅ JSON | `201` + new card |
| `POST` | `/api/transactions` | Simulate a purchase | ✅ JSON | `200` SUCCESS or `422` DECLINED |
| `GET` | `/api/transactions/card/{cardId}` | Get transaction history for a card | ❌ None | `200` + transaction list |

### Backend Package Structure

```
com.finvault.backend
│
├── 📂 controller/                    ← 🎯 HTTP layer — receives requests, returns responses
│   ├── AuthController.java           ← POST /api/auth/register, POST /api/auth/login
│   ├── VirtualCardController.java    ← GET  /api/cards/user/{userId}, POST /api/cards
│   └── TransactionController.java    ← POST /api/transactions, GET /api/transactions/card/{id}
│
├── 📂 dto/                           ← 📦 Data Transfer Objects — API contract
│   ├── UserRegistrationDto.java      ← Inbound: { username, email, password }
│   ├── LoginRequestDto.java          ← Inbound: { email, password }
│   ├── LoginResponseDto.java         ← Outbound: { userId, username, email, message }
│   ├── VirtualCardResponseDto.java   ← Outbound: { id, cardNumber, cvv, dailyLimit, balance }
│   ├── TransactionRequestDto.java    ← Inbound: { cardId, amount, merchantName }
│   └── TransactionResponseDto.java   ← Outbound: { id, cardId, amount, merchantName, timestamp, status }
│
├── 📂 service/                       ← ⚙️ Business logic — called by controllers
│   ├── UserService.java              ← Registration + login logic + BCrypt hashing
│   ├── VirtualCardService.java       ← Card creation + fetching + Entity→DTO mapping
│   └── TransactionService.java       ← Transaction processing + daily-limit check
│
├── 📂 repository/                    ← 💾 Data access — talks to MySQL
│   ├── UserRepository.java           ← JpaRepository<User, Long>
│   ├── VirtualCardRepository.java    ← JpaRepository<VirtualCard, Long>
│   └── TransactionRepository.java    ← JpaRepository<Transaction, Long> + custom finder
│
├── 📂 entity/                        ← 💾 JPA domain model
│   ├── User.java                     ← @Entity → users table
│   ├── VirtualCard.java              ← @Entity → virtual_cards table
│   └── Transaction.java              ← @Entity → transactions table
│
└── 📂 config/
    └── SecurityConfig.java           ← 🔒 BCryptPasswordEncoder bean + open filter chain
```

---

## 🔄 The 3-Layer Request Flow

Every API request in FinVault follows this strict, one-directional flow:

```
  📨 HTTP Request
      │
      ▼
  ┌──────────────────────────────────────────────────────────┐
  │  🎯 CONTROLLER LAYER                                     │
  │  @RestController  @RequestMapping("/api/...")             │
  │                                                          │
  │  • Receives HTTP request                                 │
  │  • Deserializes JSON body → DTO (via Jackson)            │
  │  • Delegates to Service                                  │
  │  • Wraps result in ResponseEntity with status code       │
  │  • Returns JSON response                                 │
  └───────────────────────┬──────────────────────────────────┘
                          │ calls
  ┌───────────────────────▼──────────────────────────────────┐
  │  ⚙️ SERVICE LAYER                                        │
  │  @Service                                                │
  │                                                          │
  │  • Contains ALL business logic                           │
  │  • Validates business rules (e.g., email uniqueness)     │
  │  • Transforms data (e.g., password → BCrypt hash)        │
  │  • Maps entities ↔ DTOs                                  │
  │  • Calls Repository for database operations              │
  └───────────────────────┬──────────────────────────────────┘
                          │ calls
  ┌───────────────────────▼──────────────────────────────────┐
  │  💾 REPOSITORY LAYER                                     │
  │  extends JpaRepository<Entity, Long>                     │
  │                                                          │
  │  • Provides CRUD operations (save, find, delete, etc.)   │
  │  • Spring Data generates SQL from method names           │
  │  • Hibernate executes SQL against MySQL                  │
  │  • Returns Entity objects to Service                     │
  └──────────────────────────────────────────────────────────┘
```

---

## 📝 Endpoint 1: Register User

### `POST /api/auth/register`

Creates a new FinVault user account. The raw password is hashed using **BCrypt (strength 10)** before being stored — plaintext passwords are **never** persisted.

### Request

```http
POST /api/auth/register HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

### Request Body Fields

| Field | Type | Required | Validation | Notes |
|:-----:|:----:|:--------:|-----------|-------|
| `username` | `string` | ✅ | Stored as-is | Display name for the user |
| `email` | `string` | ✅ | Must be unique in `users` table | Used for login and notifications |
| `password` | `string` | ✅ | Hashed by BCrypt before storage | Plaintext is **immediately discarded** after hashing |

### Success Response — `201 Created`

```json
{
  "message": "User registered successfully",
  "userId": 1
}
```

### Error Response — `400 Bad Request`

```json
{
  "error": "Email is already registered: john@example.com"
}
```

### Internal Flow Diagram

```
POST /api/auth/register
       │
       ▼
┌─ AuthController ─────────────────────────────────────────────┐
│  @PostMapping("/register")                                   │
│  register(@RequestBody UserRegistrationDto dto)              │
│       │                                                      │
│       │  try {                                               │
│       ▼                                                      │
│  ┌─ UserService ──────────────────────────────────────────┐  │
│  │  1. existsByEmail(dto.getEmail())                      │  │
│  │     └── If true → throw IllegalArgumentException       │  │
│  │                                                        │  │
│  │  2. passwordEncoder.encode(dto.getPassword())          │  │
│  │     └── "SecurePass123" → "$2a$10$N9qo8uLOi..."       │  │
│  │                                                        │  │
│  │  3. userRepository.save(user)                          │  │
│  │     └── INSERT INTO users VALUES (...)                 │  │
│  │                                                        │  │
│  │  4. return saved.getId()  →  1                         │  │
│  └────────────────────────────────────────────────────────┘  │
│       │                                                      │
│       ▼                                                      │
│  ResponseEntity.status(201).body({                           │
│    "message": "User registered successfully",                │
│    "userId": 1                                               │
│  })                                                          │
└──────────────────────────────────────────────────────────────┘
```

---

## 🔐 Endpoint 2: Login User

### `POST /api/auth/login`

Authenticates an existing user by verifying their email and password against the BCrypt hash stored in the database.

### Request

```http
POST /api/auth/login HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

### Request Body Fields

| Field | Type | Required | Notes |
|:-----:|:----:|:--------:|-------|
| `email` | `string` | ✅ | Must match a registered email |
| `password` | `string` | ✅ | Compared to BCrypt hash via `BCryptPasswordEncoder.matches()` |

### Success Response — `200 OK`

```json
{
  "userId": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "message": "Login successful"
}
```

> 📌 The frontend stores `{ userId, username, email }` in `sessionStorage` under the key `finvault_user`. This session data powers the `AuthGuard` and dashboard user display.

### Error Response — `400 Bad Request`

```json
{
  "error": "Invalid email or password"
}
```

### Internal Flow

```
POST /api/auth/login
       │
       ▼
┌─ AuthController ─────────────────────────────────────────────┐
│  @PostMapping("/login")                                      │
│  login(@RequestBody LoginRequestDto dto)                     │
│       │                                                      │
│       ▼                                                      │
│  ┌─ UserService ──────────────────────────────────────────┐  │
│  │  1. findByEmail(dto.getEmail())                        │  │
│  │     └── Not found → throw IllegalArgumentException     │  │
│  │                                                        │  │
│  │  2. passwordEncoder.matches(dto.getPassword(),         │  │
│  │                             user.getPasswordHash())    │  │
│  │     └── No match → throw IllegalArgumentException      │  │
│  │                                                        │  │
│  │  3. return LoginResponseDto {                          │  │
│  │       userId, username, email, "Login successful"      │  │
│  │     }                                                  │  │
│  └────────────────────────────────────────────────────────┘  │
│       │                                                      │
│       ▼                                                      │
│  ResponseEntity.ok(loginResponse)  →  200 OK                 │
└──────────────────────────────────────────────────────────────┘
```

### cURL Example

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "john@example.com", "password": "SecurePass123"}'
```

---

## 🃏 Endpoint 3: Get User's Virtual Cards

### `GET /api/cards/user/{userId}`

Returns all virtual cards owned by the specified user. The **CVV** and raw **user FK** are deliberately excluded from the response DTO — only display-safe fields are exposed.

### Request

```http
GET /api/cards/user/1 HTTP/1.1
Host: localhost:8080
```

### Path Parameter

| Parameter | Type | Location | Required | Description |
|:---------:|:----:|:--------:|:--------:|-------------|
| `userId` | `Long` | URL path | ✅ | The database ID of the target user |

### Success Response — `200 OK`

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

> 📌 Returns `[]` (empty array) if the user has no cards — **never** returns `null`.

### Success Response — `200 OK`

```json
[
  {
    "id": 1,
    "cardNumber": "4111111111111111",
    "cvv": "847",
    "dailyLimit": 500.00,
    "balance": 75.00
  }
]
```

> 📌 Returns `[]` (empty array) if the user has no cards — **never** returns `null`.

### Response Fields

| Field | Type | Description | Sensitive? |
|:-----:|:----:|-------------|:----------:|
| `id` | `Long` | Card's database ID | No |
| `cardNumber` | `String` | 16-digit card number | Moderate |
| `cvv` | `String` | 3-digit security code | ⚠️ Present (display only in UI) |
| `dailyLimit` | `BigDecimal` | Maximum daily spend cap | No |
| `balance` | `BigDecimal` | Amount spent today (resets per day in future sprint) | No |
| ~~`userId`~~ | ~~`Long`~~ | ~~Foreign key to users table~~ | ⛔ **EXCLUDED** |
| ~~`status`~~ | ~~`String`~~ | ~~Card lifecycle state~~ | ⛔ **EXCLUDED** (future sprint) |

### Internal Flow Diagram

```
GET /api/cards/user/1
       │
       ▼
┌─ VirtualCardController ──────────────────────────────────────┐
│  @GetMapping("/user/{userId}")                               │
│  getCardsByUser(@PathVariable Long userId)                   │
│       │                                                      │
│       ▼                                                      │
│  ┌─ VirtualCardService ───────────────────────────────────┐  │
│  │  1. virtualCardRepository.findByUserId(1)              │  │
│  │     └── SELECT * FROM virtual_cards WHERE user_id = 1  │  │
│  │                                                        │  │
│  │  2. cards.stream().map(card → new VirtualCardResponseDto( │
│  │        card.getId(),                                   │  │
│  │        card.getCardNumber(),                           │  │
│  │        card.getDailyLimit(),                           │  │
│  │        card.getStatus().name()                         │  │
│  │     ))                                                 │  │
│  │     └── Entity → DTO (CVV and userId stripped out!)    │  │
│  │                                                        │  │
│  │  3. return List<VirtualCardResponseDto>                │  │
│  └────────────────────────────────────────────────────────┘  │
│       │                                                      │
│       ▼                                                      │
│  ResponseEntity.ok(cards)  →  200 OK + JSON array            │
└──────────────────────────────────────────────────────────────┘
```

---

## 🛡️ Why DTOs? — Anti-Pattern Prevention

### The Problem: Returning `@Entity` Directly

Returning JPA `@Entity` objects directly from a controller is a **dangerous anti-pattern**:

```
  ❌ BAD: Controller returns Entity directly
  ──────────────────────────────────────────
  @GetMapping("/user/{id}")
  public User getUser(@PathVariable Long id) {
      return userRepository.findById(id).get();
  }

  Response:
  {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "passwordHash": "$2a$10$N9qo8...",     ← 🚨 PASSWORD HASH EXPOSED!
    "virtualCards": [
      {
        "cvv": "123",                       ← 🚨 CVV EXPOSED!
        "user": {
          "virtualCards": [                  ← 🚨 INFINITE LOOP → StackOverflowError!
            ...
```

### The 3 Dangers

| # | Danger | Without DTOs | With DTOs |
|:-:|--------|:------------:|:---------:|
| 1 | **Over-exposure** | `passwordHash` and `cvv` leak into JSON | ✅ Only safe fields in DTO |
| 2 | **Tight coupling** | DB column rename breaks the API | ✅ DTO is independent of schema |
| 3 | **Circular serialization** | `User ↔ VirtualCard` bidirectional causes `StackOverflowError` | ✅ DTO has no JPA relationships |

### FinVault's DTO Strategy

```
  ✅ GOOD: Controller returns DTO
  ──────────────────────────────

  Entity (DB layer)              DTO (API layer)
  ┌──────────────────┐           ┌──────────────────┐
  │ VirtualCard      │           │ VirtualCardResponse│
  │ ─────────────    │   map()   │ ──────────────────│
  │ id              │──────────►│ id                │
  │ cardNumber      │──────────►│ cardNumber        │
  │ dailyLimit      │──────────►│ dailyLimit        │
  │ status          │──────────►│ status            │
  │ cvv             │     ✖     │                   │  ← CVV not mapped
  │ user (FK)       │     ✖     │                   │  ← user FK not mapped
  │ expiryDate      │     ✖     │                   │  ← expiry not mapped
  │ createdAt       │     ✖     │                   │  ← timestamp not mapped
  └──────────────────┘           └──────────────────┘
```

---

## 🔒 Security Configuration (Sprint 1)

### Current State: Open Endpoints

Spring Security was added to the classpath in Sprint 1 to provide `BCryptPasswordEncoder`. A `SecurityConfig` class establishes the baseline:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();     // Strength 10 (default)
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())       // Correct for stateless REST APIs
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()        // Sprint 1: all endpoints open
            );
        return http.build();
    }
}
```

### What Each Setting Means

| Setting | Current Value | Purpose | Future Change |
|---------|:------------:|---------|:-------------:|
| `BCryptPasswordEncoder` | Strength 10 | Hashes passwords with adaptive work factor | No change needed |
| `csrf.disable()` | Disabled | Correct for stateless REST APIs (no cookies/sessions) | Stays disabled |
| `anyRequest().permitAll()` | All open | No authentication required for Sprint 1 | 🔜 JWT filter in security sprint |

### How BCrypt Works

```
  Input: "SecurePass123"
          │
          ▼
  ┌─ BCryptPasswordEncoder.encode() ─────────────────────┐
  │                                                       │
  │  1. Generate random 16-byte SALT                      │
  │  2. Hash password + salt using Blowfish cipher        │
  │  3. Repeat 2^10 (1024) times  ← cost factor "10"     │
  │  4. Produce 60-character string:                      │
  │     $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldG...  │
  │                                                       │
  └───────────────────────────────────────────────────────┘
          │
          ▼
  Stored in: users.password_hash column
  
  ⚠️ KEY PROPERTY: Same password generates DIFFERENT hashes each time
     (because the salt is random). BCrypt.matches() handles comparison.
```

---

## 🧪 Testing the API with cURL

### Register a New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecurePass123"
  }'
```

**Expected:** `201 Created`
```json
{ "message": "User registered successfully", "userId": 1 }
```

### Register with Duplicate Email (Error Case)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "janedoe",
    "email": "john@example.com",
    "password": "AnotherPass456"
  }'
```

**Expected:** `400 Bad Request`
```json
{ "error": "Email is already registered: john@example.com" }
```

### Get Cards for User #1

```bash
curl http://localhost:8080/api/cards/user/1
```

**Expected:** `200 OK`
```json
[
  { "id": 1, "cardNumber": "4111111111111111", "dailyLimit": 500.00, "status": "ACTIVE" }
]
```

---

## 📚 Glossary

| Term | Definition |
|------|-----------|
| **REST** | Representational State Transfer — an architectural style for web APIs using HTTP methods |
| **Endpoint** | A specific URL + HTTP method combination that performs an action (e.g., `POST /api/auth/register`) |
| **JSON** | JavaScript Object Notation — lightweight data format for API request/response bodies |
| **DTO** | Data Transfer Object — a simple Java class used to define the shape of API request/response data |
| **Controller** | A Spring class annotated with `@RestController` that handles HTTP requests |
| **Service** | A Spring class annotated with `@Service` that contains business logic |
| **Repository** | A Spring Data interface that provides database CRUD operations |
| **BCrypt** | A password hashing algorithm with adaptive cost factor — industry standard for storing passwords |
| **JWT** | JSON Web Token — a self-contained token for stateless authentication (planned for security sprint) |
| **CSRF** | Cross-Site Request Forgery — an attack irrelevant to stateless REST APIs (correctly disabled) |
| **ResponseEntity** | Spring's wrapper for HTTP responses — includes status code, headers, and body |
| **@RequestBody** | Spring annotation that deserializes JSON request body into a Java object (DTO) |
| **@PathVariable** | Spring annotation that extracts a variable from the URL path (e.g., `{userId}`) |
| **Idempotent** | An operation that produces the same result regardless of how many times it's called (GET, PUT, DELETE) |

---

## 🆕 Endpoint 4: Create Virtual Card

### `POST /api/cards`

Creates a new virtual card for a registered user with a configurable daily spending limit.

### Request Body

| Field | Type | Required | Description |
|:-----:|:----:|:--------:|-------------|
| `userId` | `Long` | ✅ | The ID of the user who will own this card |
| `dailyLimit` | `BigDecimal` | ✅ | Maximum daily spend limit (e.g., `500.00`) |

```json
{
  "userId": 1,
  "dailyLimit": 500.00
}
```

### Success Response — `201 CREATED`

```json
{
  "id": 3,
  "cardNumber": "4829103746582910",
  "dailyLimit": 500.00,
  "status": "ACTIVE"
}
```

### Error Response — `500 Internal Server Error`

Returned when the `userId` does not match any existing user.

```json
{
  "error": "User not found with ID: 99"
}
```

### Auto-Generation Logic

| Field | Logic |
|:-----:|-------|
| `cardNumber` | Random 16-digit numeric string generated via `java.util.Random` |
| `cvv` | Random 3-digit string (`000`–`999`) generated via `String.format("%03d", ...)` |
| `expiryDate` | Calculated as **current date + 3 years** using `LocalDate.now().plusYears(3)` |
| `balance` | Defaults to `0.0` |
| `status` | Defaults to `ACTIVE` |

### cURL Example

```bash
curl -X POST http://localhost:8080/api/cards \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "dailyLimit": 500.00}'
```

---

## 💳 Endpoint 5: Simulate Transaction

### `POST /api/transactions`

Simulates a spend transaction against a virtual card. The system checks whether the card's daily limit would be exceeded and either **approves** or **declines** the transaction.

### Request Body

```json
{
  "cardId": 1,
  "amount": 75.00,
  "merchantName": "Amazon"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `cardId` | Long | ✅ | ID of the virtual card to charge |
| `amount` | BigDecimal | ✅ | Spend amount (must be > 0) |
| `merchantName` | String | ✅ | Name of the merchant |

### Approval / Decline Logic

```
projectedBalance = card.balance + request.amount

IF projectedBalance <= card.dailyLimit
    → status = SUCCESS
    → card.balance is updated to projectedBalance
    → Transaction saved with status SUCCESS

IF projectedBalance > card.dailyLimit
    → status = DECLINED
    → card.balance is NOT modified
    → Transaction saved with status DECLINED
```

### Response — SUCCESS (`200 OK`)

```json
{
  "id": 1,
  "cardId": 1,
  "amount": 75.00,
  "merchantName": "Amazon",
  "timestamp": "2026-05-15T14:30:00",
  "status": "SUCCESS"
}
```

### Response — DECLINED (`422 Unprocessable Entity`)

```json
{
  "id": 2,
  "cardId": 1,
  "amount": 9999.00,
  "merchantName": "LuxuryStore",
  "timestamp": "2026-05-15T14:31:00",
  "status": "DECLINED"
}
```

### cURL Examples

```bash
# Approved transaction
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"cardId": 1, "amount": 50.00, "merchantName": "Starbucks"}'

# Declined transaction (exceeds daily limit)
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"cardId": 1, "amount": 99999.00, "merchantName": "OverLimit Corp"}'
```

---

## 📜 Endpoint 6: Get Transactions by Card

### `GET /api/transactions/card/{cardId}`

Returns all transactions recorded against a specific virtual card, ordered newest-first. Used by the Angular dashboard's **Transactions** tab to build the full history table.

### Request

```http
GET /api/transactions/card/1 HTTP/1.1
Host: localhost:8080
```

### Path Parameter

| Parameter | Type | Location | Required | Description |
|:---------:|:----:|:--------:|:--------:|-------------|
| `cardId` | `Long` | URL path | ✅ | The database ID of the virtual card |

### Success Response — `200 OK`

```json
[
  {
    "id": 3,
    "cardId": 1,
    "amount": 50.00,
    "merchantName": "Coffee Shop",
    "timestamp": "2026-05-16T14:30:00",
    "status": "SUCCESS"
  },
  {
    "id": 1,
    "cardId": 1,
    "amount": 9999.00,
    "merchantName": "OverLimit Corp",
    "timestamp": "2026-05-16T10:00:00",
    "status": "DECLINED"
  }
]
```

> 📌 Returns `[]` when no transactions exist for this card. Ordered **newest first** (descending by `timestamp`).

### Response Fields

| Field | Type | Description |
|:-----:|:----:|---------|
| `id` | `Long` | Transaction's database ID |
| `cardId` | `Long` | ID of the card this transaction belongs to |
| `amount` | `BigDecimal` | Transaction amount |
| `merchantName` | `String` | Merchant the transaction was attempted at |
| `timestamp` | `LocalDateTime` | When the transaction was recorded (`@PrePersist`) |
| `status` | `String` | `SUCCESS` or `DECLINED` |

### Internal Flow

```
GET /api/transactions/card/1
       │
       ▼
┌─ TransactionController ──────────────────────────────────────┐
│  @GetMapping("/card/{cardId}")                               │
│  getByCard(@PathVariable Long cardId)                        │
│       │                                                      │
│       ▼                                                      │
│  ┌─ TransactionService ───────────────────────────────────┐  │
│  │  @Transactional(readOnly = true)                       │  │
│  │  findByVirtualCardIdOrderByTimestampDesc(cardId)       │  │
│  │     └── SELECT * FROM transactions                     │  │
│  │            WHERE virtual_card_id = 1                   │  │
│  │            ORDER BY timestamp DESC                     │  │
│  │                                                        │  │
│  │  stream().map(tx → toResponseDto(tx)).toList()         │  │
│  └────────────────────────────────────────────────────────┘  │
│       │                                                      │
│       ▼                                                      │
│  ResponseEntity.ok(transactions)  →  200 OK + JSON array     │
└──────────────────────────────────────────────────────────────┘
```

> ⚠️ **Implementation note:** `@Transactional(readOnly = true)` is required on this method because `spring.jpa.open-in-view=false` closes the Hibernate session before the service returns. Without it, accessing `tx.getVirtualCard().getId()` on the lazy proxy inside `toResponseDto()` would throw a `LazyInitializationException`.

### cURL Example

```bash
curl http://localhost:8080/api/transactions/card/1
```

---

<p align="center">
  <b>📡 FinVault REST API Documentation</b><br>
  <sub>Sprint 1 — SCRUM-14 | Sprint 2 — SCRUM-16, SCRUM-17 | Hardening — SCRUM-18</sub><br>
  <sub>Part of the <a href="ARCHITECTURE.md">FinVault Documentation Suite</a></sub>
</p>

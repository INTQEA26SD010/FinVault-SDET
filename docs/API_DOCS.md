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
| 7 | [Endpoint 2: Get User's Virtual Cards](#-endpoint-2-get-users-virtual-cards) | `GET /api/cards/user/{userId}` |
| 8 | [Why DTOs? — Anti-Pattern Prevention](#-why-dtos--anti-pattern-prevention) | Why we never return @Entity directly |
| 9 | [Security Configuration (Sprint 1)](#-security-configuration-sprint-1) | BCrypt + permitAll baseline |
| 10 | [Testing the API with cURL](#-testing-the-api-with-curl) | Ready-to-use commands |
| 11 | [Glossary](#-glossary) | Key API terms |

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
| `200` | OK | Successfully retrieved cards (`GET /api/cards/user/{id}`) |
| `201` | Created | Successfully registered a new user (`POST /api/auth/register`) |
| `400` | Bad Request | Email already registered; invalid input data |
| `401` | Unauthorized | Invalid/missing JWT token (future sprint) |
| `404` | Not Found | Resource doesn't exist (future sprint) |
| `500` | Internal Server Error | Unhandled server exception (should never happen in production) |

---

## 🗺️ FinVault API Overview

### Quick Reference — All Endpoints

| Method | Endpoint | Purpose | Request Body | Response |
|:------:|----------|---------|:------------:|----------|
| `POST` | `/api/auth/register` | Register new user | ✅ JSON | `201` + userId |
| `GET` | `/api/cards/user/{userId}` | Get user's cards | ❌ None | `200` + card list |

### Backend Package Structure

```
com.finvault.backend
│
├── 📂 controller/                    ← 🎯 HTTP layer — receives requests, returns responses
│   ├── AuthController.java           ← POST /api/auth/register
│   └── VirtualCardController.java    ← GET  /api/cards/user/{userId}
│
├── 📂 dto/                           ← 📦 Data Transfer Objects — API contract
│   ├── UserRegistrationDto.java      ← Inbound: { username, email, password }
│   └── VirtualCardResponseDto.java   ← Outbound: { id, cardNumber, dailyLimit, status }
│
├── 📂 service/                       ← ⚙️ Business logic — called by controllers
│   ├── UserService.java              ← Registration logic + BCrypt hashing
│   └── VirtualCardService.java       ← Card fetching + Entity→DTO mapping
│
├── 📂 repository/                    ← 💾 Data access — talks to MySQL
│   ├── UserRepository.java           ← JpaRepository<User, Long>
│   └── VirtualCardRepository.java    ← JpaRepository<VirtualCard, Long>
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

## 🃏 Endpoint 2: Get User's Virtual Cards

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

### Response Fields

| Field | Type | Description | Sensitive? |
|:-----:|:----:|-------------|:----------:|
| `id` | `Long` | Card's database ID | No |
| `cardNumber` | `String` | 16-digit card number | Moderate |
| `dailyLimit` | `BigDecimal` | Maximum daily spend in base currency | No |
| `status` | `String` | One of: `ACTIVE`, `FROZEN`, `EXPIRED`, `CANCELLED` | No |
| ~~`cvv`~~ | ~~`String`~~ | ~~3-digit security code~~ | ⛔ **EXCLUDED** |
| ~~`userId`~~ | ~~`Long`~~ | ~~Foreign key to users table~~ | ⛔ **EXCLUDED** |

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

## 🆕 Endpoint 3: Create Virtual Card (Sprint 2 — SCRUM-16)

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

<p align="center">
  <b>📡 FinVault REST API Documentation</b><br>
  <sub>Sprint 1 — SCRUM-14 | Sprint 2 — SCRUM-16</sub><br>
  <sub>Part of the <a href="ARCHITECTURE.md">FinVault Documentation Suite</a></sub>
</p>

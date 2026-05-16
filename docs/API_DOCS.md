<p align="center">
  <img src="https://img.shields.io/badge/REST%20API-Spring%20Boot%204.0.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Format-JSON-000000?style=for-the-badge&logo=json&logoColor=white" />
  <img src="https://img.shields.io/badge/Endpoints-8-blue?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Port-8080-FF6B6B?style=for-the-badge" />
</p>

# 📡 FinVault — REST API Documentation

> **Base URL:** `http://localhost:8080` | **Format:** JSON | **Auth:** Open (permitAll — JWT-ready architecture)

---

## 📑 Table of Contents

| # | Section |
|:-:|---------|
| 1 | [REST API Theory](#-rest-api-theory) |
| 2 | [HTTP Methods](#-http-methods) |
| 3 | [HTTP Status Codes Used](#-http-status-codes-used) |
| 4 | [All Endpoints Quick Reference](#-all-endpoints-quick-reference) |
| 5 | [The 3-Layer Request Flow](#-the-3-layer-request-flow) |
| 6 | [Endpoint 1: Register User](#-endpoint-1-register-user) |
| 7 | [Endpoint 2: Login User](#-endpoint-2-login-user) |
| 8 | [Endpoint 3: Get User's Virtual Cards](#-endpoint-3-get-users-virtual-cards) |
| 9 | [Endpoint 4: Create Virtual Card](#-endpoint-4-create-virtual-card) |
| 10 | [Endpoint 5: Toggle Card Status](#-endpoint-5-toggle-card-status) |
| 11 | [Endpoint 6: Delete Virtual Card](#-endpoint-6-delete-virtual-card) |
| 12 | [Endpoint 7: Simulate Transaction](#-endpoint-7-simulate-transaction) |
| 13 | [Endpoint 8: Get Transactions by Card](#-endpoint-8-get-transactions-by-card) |
| 14 | [Why DTOs? — Interview Deep-Dive](#-why-dtos--interview-deep-dive) |
| 15 | [Security Configuration](#-security-configuration) |
| 16 | [Testing with cURL](#-testing-with-curl) |

---

## 📖 REST API Theory

**REST** (Representational State Transfer) is an architectural style for web APIs. Key principles:

| Principle | FinVault Implementation |
|:---------:|-------------------------|
| **Client-Server** | Angular (4200) ↔ Spring Boot (8080) |
| **Stateless** | Each request is self-contained — no server-side sessions |
| **Uniform Interface** | Consistent `/api/` prefix, standard HTTP methods |
| **Layered System** | CORS-configured; load-balancer compatible |

---

## 🔤 HTTP Methods

| Method | CRUD | Purpose | Idempotent | FinVault Usage |
|:------:|:----:|---------|:----------:|:--------------:|
| `GET` | Read | Retrieve data | ✅ Yes | Fetch cards, fetch transactions |
| `POST` | Create | Submit new data | ❌ No | Register, login, create card, simulate transaction |
| `PUT` | Update | Modify existing resource | ✅ Yes | Toggle card freeze/unfreeze |
| `DELETE` | Delete | Remove a resource | ✅ Yes | Delete a virtual card |

> 🎓 **Interview Tip:** GET and DELETE are idempotent — calling them N times produces the same result. POST is NOT — calling register 100 times creates 100 users (or fails on uniqueness constraint).

---

## 📊 HTTP Status Codes Used

| Code | Name | When FinVault Returns It |
|:----:|------|--------------------------|
| `200` | OK | Cards retrieved, login success, transaction processed (SUCCESS) |
| `201` | Created | User registered, card created |
| `204` | No Content | Card successfully deleted |
| `400` | Bad Request | Email already registered |
| `401` | Unauthorized | Invalid login credentials (wrong email or password) |
| `404` | Not Found | Card ID does not exist (toggle/delete) |
| `422` | Unprocessable Entity | Transaction DECLINED — daily limit exceeded |

---

## 🗺️ All Endpoints Quick Reference

| # | Method | Endpoint | Controller | Purpose | Success | Error |
|:-:|:------:|----------|:----------:|---------|:-------:|:-----:|
| 1 | `POST` | `/api/auth/register` | AuthController | Register new user | `201` | `400` |
| 2 | `POST` | `/api/auth/login` | AuthController | Login existing user | `200` | `401` |
| 3 | `GET` | `/api/cards/user/{userId}` | VirtualCardController | Get user's cards | `200` | — |
| 4 | `POST` | `/api/cards` | VirtualCardController | Create virtual card | `201` | `404` |
| 5 | `PUT` | `/api/cards/{id}/toggle` | VirtualCardController | Freeze/Unfreeze | `200` | `404` |
| 6 | `DELETE` | `/api/cards/{id}` | VirtualCardController | Delete card | `204` | `404` |
| 7 | `POST` | `/api/transactions` | TransactionController | Simulate purchase | `200` | `422` |
| 8 | `GET` | `/api/transactions/card/{cardId}` | TransactionController | Transaction history | `200` | — |

---

## 🧱 The 3-Layer Request Flow

Every request traverses three layers before reaching the database:

```
   HTTP Request
       │
       ▼
┌─────────────────────┐
│   🎯 CONTROLLER      │  Receives @RequestBody, validates path params
│   @RestController    │  Returns ResponseEntity<> with status code
└──────────┬──────────┘
           │ calls
           ▼
┌─────────────────────┐
│   ⚙️ SERVICE          │  Business logic + @Transactional
│   @Service           │  BCrypt hashing, daily-limit check, DTO mapping
└──────────┬──────────┘
           │ calls
           ▼
┌─────────────────────┐
│   💾 REPOSITORY      │  Spring Data JPA interface
│   JpaRepository<>    │  Derived query methods → auto-generated SQL
└──────────┬──────────┘
           │
           ▼
       🗄️ MySQL
```

---

## 📝 Endpoint 1: Register User

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **URL** | `/api/auth/register` |
| **Controller** | `AuthController` |
| **Service** | `UserService.registerUser()` |
| **Logic** | Check email uniqueness → BCrypt hash password → save to DB |

### Request Body

```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

### Response — 201 Created

```json
{
  "message": "User registered successfully",
  "userId": 1
}
```

### Response — 400 Bad Request

```json
{
  "error": "Email is already registered: john@example.com"
}
```

### 🎓 Why BCrypt? (Interview)

> BCrypt uses an adaptive cost factor (default 10 = 2^10 iterations). As hardware improves, increase the cost to maintain security. The 60-character output includes the salt, making rainbow table attacks impossible.

---

## 📝 Endpoint 2: Login User

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **URL** | `/api/auth/login` |
| **Controller** | `AuthController` |
| **Service** | `UserService.loginUser()` |
| **Logic** | Find user by email → `passwordEncoder.matches()` → return session data |

### Request Body

```json
{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

### Response — 200 OK

```json
{
  "userId": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "message": "Login successful"
}
```

### Response — 401 Unauthorized

```json
{
  "error": "Invalid email or password"
}
```

> 🎓 **Security Note:** The error message is deliberately vague ("Invalid email or password") rather than revealing whether the email exists. This prevents user enumeration attacks.

---

## 📝 Endpoint 3: Get User's Virtual Cards

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/cards/user/{userId}` |
| **Controller** | `VirtualCardController` |
| **Service** | `VirtualCardService.getCardsByUserId()` |
| **Logic** | Query by FK → map each entity to `VirtualCardResponseDto` |

### Response — 200 OK

```json
[
  {
    "id": 1,
    "cardNumber": "4829173650284916",
    "cvv": "742",
    "dailyLimit": 500.00,
    "balance": 125.50,
    "status": "ACTIVE",
    "vendorName": "Amazon"
  },
  {
    "id": 2,
    "cardNumber": "5192847365102938",
    "cvv": "318",
    "dailyLimit": 200.00,
    "balance": 0.00,
    "status": "FROZEN",
    "vendorName": "Netflix"
  }
]
```

> Returns an empty array `[]` if the user has no cards.

---

## 📝 Endpoint 4: Create Virtual Card

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **URL** | `/api/cards` |
| **Controller** | `VirtualCardController` |
| **Service** | `VirtualCardService.createVirtualCard()` |
| **Logic** | Validate user exists → generate 16-digit number + 3-digit CVV → set expiry (now + 3 years) → save |

### Request Body

```json
{
  "userId": 1,
  "dailyLimit": 500.00,
  "vendorName": "Amazon"
}
```

### Response — 201 Created

```json
{
  "id": 3,
  "cardNumber": "7291038465192837",
  "cvv": "159",
  "dailyLimit": 500.00,
  "balance": 0.00,
  "status": "ACTIVE",
  "vendorName": "Amazon"
}
```

### Auto-Generated Fields

| Field | Generation Logic |
|-------|-----------------|
| `cardNumber` | Random 16 digits (via `Random.nextInt(10)` × 16) |
| `cvv` | Random 3 digits, zero-padded (`String.format("%03d", ...)`) |
| `expiryDate` | `LocalDate.now().plusYears(3)` |
| `balance` | `BigDecimal.ZERO` (no spending yet) |
| `status` | `CardStatus.ACTIVE` |

---

## 📝 Endpoint 5: Toggle Card Status

| Property | Value |
|----------|-------|
| **Method** | `PUT` |
| **URL** | `/api/cards/{id}/toggle` |
| **Controller** | `VirtualCardController` |
| **Service** | `VirtualCardService.toggleCardStatus()` |
| **Logic** | ACTIVE → FROZEN or FROZEN → ACTIVE; annotated `@Transactional` |

### Request

No request body required. The card ID is in the URL path.

### Response — 200 OK

```json
{
  "id": 1,
  "cardNumber": "4829173650284916",
  "cvv": "742",
  "dailyLimit": 500.00,
  "balance": 125.50,
  "status": "FROZEN",
  "vendorName": "Amazon"
}
```

### Response — 404 Not Found

Empty body (card ID does not exist).

### 🎓 Why PUT, Not PATCH? (Interview)

> PUT replaces the resource's state. Since the toggle is a **complete state transition** (ACTIVE→FROZEN or FROZEN→ACTIVE), PUT is semantically correct. PATCH would be used for partial updates (e.g., changing only `dailyLimit`).

---

## 📝 Endpoint 6: Delete Virtual Card

| Property | Value |
|----------|-------|
| **Method** | `DELETE` |
| **URL** | `/api/cards/{id}` |
| **Controller** | `VirtualCardController` |
| **Service** | `VirtualCardService.deleteCard()` |
| **Logic** | Check exists → `deleteById()` → Cascade deletes child transactions |

### Response — 204 No Content

No response body on success.

### Response — 404 Not Found

Empty body (card ID does not exist).

### 🎓 Why 204 Instead of 200? (Interview)

> 204 means "success, but there's nothing to return." Since the resource no longer exists after deletion, returning its data would be misleading. This is the REST convention for DELETE.

---

## 📝 Endpoint 7: Simulate Transaction

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **URL** | `/api/transactions` |
| **Controller** | `TransactionController` |
| **Service** | `TransactionService.processTransaction()` |
| **Logic** | `if (card.balance + amount <= dailyLimit)` → SUCCESS else DECLINED |

### Request Body

```json
{
  "cardId": 1,
  "amount": 75.00,
  "merchantName": "Coffee Shop"
}
```

### Response — 200 OK (Approved)

```json
{
  "id": 5,
  "cardId": 1,
  "amount": 75.00,
  "merchantName": "Coffee Shop",
  "timestamp": "2026-05-16T14:30:22.123",
  "status": "SUCCESS"
}
```

### Response — 422 Unprocessable Entity (Declined)

```json
{
  "id": 6,
  "cardId": 1,
  "amount": 600.00,
  "merchantName": "Electronics Store",
  "timestamp": "2026-05-16T14:31:05.456",
  "status": "DECLINED"
}
```

### Approval Logic (Pseudocode)

```
projectedBalance = card.balance + request.amount

if (projectedBalance <= card.dailyLimit):
    transaction.status = SUCCESS
    card.balance = projectedBalance    ← balance incremented
    save(card)
else:
    transaction.status = DECLINED      ← balance unchanged

save(transaction)                       ← always persisted for audit
```

> 🎓 **Key insight:** Both approved AND declined transactions are saved to the database. This creates a complete audit trail — essential for financial applications.

---

## 📝 Endpoint 8: Get Transactions by Card

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/transactions/card/{cardId}` |
| **Controller** | `TransactionController` |
| **Service** | `TransactionService.getTransactionsByCardId()` |
| **Repository Method** | `findByVirtualCardIdOrderByTimestampDesc` |

### Response — 200 OK

```json
[
  {
    "id": 6,
    "cardId": 1,
    "amount": 600.00,
    "merchantName": "Electronics Store",
    "timestamp": "2026-05-16T14:31:05.456",
    "status": "DECLINED"
  },
  {
    "id": 5,
    "cardId": 1,
    "amount": 75.00,
    "merchantName": "Coffee Shop",
    "timestamp": "2026-05-16T14:30:22.123",
    "status": "SUCCESS"
  }
]
```

> Results are sorted **newest-first** by the repository's `OrderByTimestampDesc` clause. Returns `[]` if no transactions exist.

---

## 🎓 Why DTOs? — Interview Deep-Dive

### The Problem: Exposing @Entity Directly

```java
// ❌ BAD — Returning the entity directly
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    return userRepo.findById(id).get();
}
// JSON output includes passwordHash! Security nightmare.
```

### The Solution: Data Transfer Objects

```java
// ✅ GOOD — Return a DTO that excludes sensitive fields
@GetMapping("/cards/user/{userId}")
public List<VirtualCardResponseDto> getCards(@PathVariable Long userId) {
    return service.getCardsByUserId(userId);  // DTO mapping happens in service
}
```

### FinVault's DTO Inventory

| DTO | Direction | Fields | Excludes |
|-----|:---------:|--------|----------|
| `UserRegistrationDto` | Inbound | username, email, password | — |
| `LoginRequestDto` | Inbound | email, password | — |
| `LoginResponseDto` | Outbound | userId, username, email, message | passwordHash |
| `CreateVirtualCardDto` | Inbound | userId, dailyLimit, vendorName | — |
| `VirtualCardResponseDto` | Outbound | id, cardNumber, cvv, dailyLimit, balance, status, vendorName | user FK object, expiryDate, createdAt |
| `TransactionRequestDto` | Inbound | cardId, amount, merchantName | — |
| `TransactionResponseDto` | Outbound | id, cardId, amount, merchantName, timestamp, status | virtualCard object reference |

### 🎓 Three Reasons DTOs Are Non-Negotiable

1. **Security** — Never expose `passwordHash`, internal FK objects, or audit timestamps in API responses
2. **Decoupling** — DB schema can change without breaking the API contract (add/remove columns freely)
3. **Over-posting Prevention** — Client cannot inject extra fields (e.g., `isAdmin: true`) into the entity

---

## 🔒 Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // strength 10 (default)
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        // ...
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())       // stateless API — no session cookies
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()        // JWT will restrict this later
            );
        return http.build();
    }
}
```

### 🎓 Why Disable CSRF? (Interview)

> CSRF protection is for **session-based** authentication where the browser auto-attaches cookies. FinVault's API is stateless — Angular sends explicit JSON payloads, not HTML form posts. CSRF attacks cannot forge JSON requests with custom headers.

---

## 🧪 Testing with cURL

```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","email":"john@example.com","password":"SecurePass123"}'

# 2. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"SecurePass123"}'

# 3. Create Card (with vendorName)
curl -X POST http://localhost:8080/api/cards \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"dailyLimit":500.00,"vendorName":"Amazon"}'

# 4. Get Cards
curl http://localhost:8080/api/cards/user/1

# 5. Toggle Freeze
curl -X PUT http://localhost:8080/api/cards/1/toggle

# 6. Delete Card
curl -X DELETE http://localhost:8080/api/cards/1

# 7. Simulate Transaction
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"cardId":1,"amount":75.00,"merchantName":"Coffee Shop"}'

# 8. Get Transaction History
curl http://localhost:8080/api/transactions/card/1
```

---

<p align="center">
  <b>📡 FinVault REST API Documentation</b><br>
  <sub>8 endpoints | 3 controllers | 7 DTOs | BCrypt + CORS security baseline</sub><br>
  <sub>Part of the <a href="../README.md">FinVault Documentation Suite</a></sub>
</p>

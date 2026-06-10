<p align="center">
  <img src="https://img.shields.io/badge/Authentication-Session%20Based-4CAF50?style=for-the-badge&logo=lock&logoColor=white" />
  <img src="https://img.shields.io/badge/Password-BCrypt%20Hashed-FF5722?style=for-the-badge&logo=security&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Security-Enabled-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" />
  <img src="https://img.shields.io/badge/JWT-Not%20Implemented-757575?style=for-the-badge" />
</p>

# 🔐 FinVault — Authentication & Authorization Documentation

> **FinVault** uses a **simple session-based authentication** pattern suitable for learning and prototyping.  
> It leverages **Spring Security** for CORS/password hashing and **Angular route guards** for frontend protection.  
> **JWT is NOT implemented** — the current architecture is "JWT-ready" for future enhancements.

---

## 📑 Table of Contents

| # | Section | Description |
|:-:|---------|-------------|
| 1 | [Authentication Overview](#-authentication-overview) | What we use vs. what we don't |
| 2 | [Authentication Flow Diagram](#-authentication-flow-diagram) | Visual step-by-step flow |
| 3 | [Backend Security Stack](#-backend-security-stack) | Spring Security configuration |
| 4 | [Password Hashing with BCrypt](#-password-hashing-with-bcrypt) | How passwords are secured |
| 5 | [Frontend Session Management](#-frontend-session-management) | How Angular manages login state |
| 6 | [Route Guards](#-route-guards) | Protecting frontend routes |
| 7 | [Registration Flow](#-registration-flow) | Complete user signup process |
| 8 | [Login Flow](#-login-flow) | Complete user authentication process |
| 9 | [Logout Flow](#-logout-flow) | How session ends |
| 10 | [Security Limitations](#-security-limitations) | Current gaps and improvements |
| 11 | [File Reference](#-file-reference) | All auth-related files |
| 12 | [Interview Q&A](#-interview-qa) | Common authentication questions |

---

## 🔑 Authentication Overview

### What We Are Using

| Feature | Technology | Description |
|:-------:|:----------:|-------------|
| ✅ Password Hashing | **BCrypt** | Passwords stored as irreversible hashes |
| ✅ CORS Protection | **Spring Security** | Only `localhost:4200` can access API |
| ✅ Session Storage | **sessionStorage** | User data stored in browser tab |
| ✅ Route Guards | **Angular CanActivate** | Blocks unauthorized route access |
| ✅ Client-Side Auth Check | **AuthService** | `isLoggedIn()` method |

### What We Are NOT Using

| Feature | Status | Why Not? |
|:-------:|:------:|----------|
| ❌ JWT Tokens | Not Implemented | Simpler for training; planned for future sprint |
| ❌ Server Sessions | Disabled | API is stateless; no `JSESSIONID` cookie |
| ❌ CSRF Protection | Disabled | Not needed for stateless REST APIs |
| ❌ OAuth2/SSO | Not Implemented | Out of scope for MVP |
| ❌ Backend Authorization | **permitAll()** | All endpoints are currently public |

---

## 🔄 Authentication Flow Diagram

### High-Level Overview

```
┌───────────────────────────────────────────────────────────────────────────────┐
│                              FINVAULT AUTH FLOW                               │
├───────────────────────────────────────────────────────────────────────────────┤
│                                                                               │
│   ┌─────────────┐         ┌─────────────────────────┐         ┌────────────┐ │
│   │   ANGULAR   │  HTTP   │      SPRING BOOT        │  JDBC   │   MYSQL    │ │
│   │  Frontend   │ ───────►│       Backend           │ ───────►│  Database  │ │
│   └─────────────┘         └─────────────────────────┘         └────────────┘ │
│         │                           │                               │        │
│         │  POST /api/auth/login     │                               │        │
│         │  {email, password}        │                               │        │
│         │ ─────────────────────────►│                               │        │
│         │                           │  Find user by email           │        │
│         │                           │ ─────────────────────────────►│        │
│         │                           │                               │        │
│         │                           │  Return User entity           │        │
│         │                           │ ◄─────────────────────────────│        │
│         │                           │                               │        │
│         │                           │  BCrypt.matches(              │        │
│         │                           │    password,                  │        │
│         │                           │    user.passwordHash          │        │
│         │                           │  )                            │        │
│         │                           │                               │        │
│         │  200 OK                   │                               │        │
│         │  {userId, username,       │                               │        │
│         │   email, message}         │                               │        │
│         │ ◄─────────────────────────│                               │        │
│         │                           │                               │        │
│         │  Store in sessionStorage  │                               │        │
│         │  ┌────────────────────┐   │                               │        │
│         │  │ finvault_user:     │   │                               │        │
│         │  │ {userId, username, │   │                               │        │
│         │  │  email}            │   │                               │        │
│         │  └────────────────────┘   │                               │        │
│         │                           │                               │        │
│         │  Navigate to /dashboard   │                               │        │
│         │                           │                               │        │
└───────────────────────────────────────────────────────────────────────────────┘
```

### Route Guard Check Flow

```
┌────────────────────────────────────────────────────────────────┐
│                    ROUTE GUARD FLOW                            │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│   User clicks /dashboard link                                  │
│            │                                                   │
│            ▼                                                   │
│   ┌────────────────────┐                                       │
│   │   authGuard()      │                                       │
│   │   CanActivateFn    │                                       │
│   └─────────┬──────────┘                                       │
│             │                                                  │
│             ▼                                                  │
│   ┌────────────────────┐                                       │
│   │ authService        │                                       │
│   │   .isLoggedIn()    │                                       │
│   └─────────┬──────────┘                                       │
│             │                                                  │
│             ▼                                                  │
│   ┌────────────────────┐                                       │
│   │ sessionStorage     │                                       │
│   │ .getItem(          │                                       │
│   │   'finvault_user') │                                       │
│   └─────────┬──────────┘                                       │
│             │                                                  │
│      ┌──────┴──────┐                                           │
│      │             │                                           │
│   HAS DATA      NO DATA                                        │
│      │             │                                           │
│      ▼             ▼                                           │
│  ┌───────┐    ┌────────────┐                                   │
│  │ true  │    │ false      │                                   │
│  │       │    │ redirect   │                                   │
│  │ Allow │    │ to /login  │                                   │
│  └───────┘    └────────────┘                                   │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

---

## 🛡️ Backend Security Stack

### Spring Security Configuration

FinVault uses `spring-boot-starter-security` with a custom `SecurityConfig` class:

**File:** `backend/src/main/java/com/finvault/backend/config/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Password encoder for hashing
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS configuration for Angular frontend
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // Security filter chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())          // Disabled for REST API
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()          // ALL endpoints PUBLIC (for now)
            );
        return http.build();
    }
}
```

### Key Configuration Decisions

| Setting | Value | Reason |
|---------|-------|--------|
| **CSRF** | `disabled` | REST API is stateless — no session cookies to protect |
| **CORS Origins** | `localhost:4200` | Only our Angular dev server can call the API |
| **Authorization** | `permitAll()` | All endpoints public (JWT will restrict this later) |
| **Password Encoder** | `BCryptPasswordEncoder` | Industry-standard one-way hashing |

---

## 🔒 Password Hashing with BCrypt

### What is BCrypt?

BCrypt is a **one-way hashing algorithm** designed specifically for passwords. It converts readable passwords into irreversible hashes.

```
┌─────────────────────────────────────────────────────────────────┐
│                        BCrypt HASHING                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Registration:                                                  │
│  ┌─────────────────┐      BCrypt       ┌────────────────────┐  │
│  │ "MyPassword123" │ ──── encode() ───►│ "$2a$10$N9qo8uL..." │  │
│  │  (raw password) │                   │  (stored in DB)    │  │
│  └─────────────────┘                   └────────────────────┘  │
│                                                                 │
│  Login:                                                         │
│  ┌─────────────────┐     BCrypt        ┌────────────────────┐  │
│  │ "MyPassword123" │ ─── matches() ───►│ "$2a$10$N9qo8uL..." │  │
│  │  (typed by user)│         │         │  (from database)   │  │
│  └─────────────────┘         │         └────────────────────┘  │
│                              │                                  │
│                              ▼                                  │
│                        ┌──────────┐                             │
│                        │  true ✓  │  (Password matches!)        │
│                        └──────────┘                             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Why BCrypt? (Interview Answer)

| Feature | Benefit |
|---------|---------|
| **One-Way** | Hash cannot be reversed → stolen hashes are useless |
| **Salt Built-In** | Each hash includes random salt → same password = different hash |
| **Slow by Design** | Cost factor makes brute-force attacks impractical |
| **Industry Standard** | Used by Netflix, GitHub, Dropbox, etc. |

### Code Usage

**Registration (hashing password):**
```java
// In UserService.registerUser()
user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
// "MyPass123" → "$2a$10$xyz..."
```

**Login (verifying password):**
```java
// In UserService.loginUser()
if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
    throw new IllegalArgumentException("Invalid email or password");
}
// Returns true if typed password matches stored hash
```

---

## 💾 Frontend Session Management

### AuthService Implementation

**File:** `frontend/src/app/services/auth.service.ts`

The `AuthService` handles all authentication operations:

| Method | Purpose |
|--------|---------|
| `register(payload)` | Calls POST `/api/auth/register` |
| `login(payload)` | Calls POST `/api/auth/login` + stores session |
| `setSession(user)` | Stores user data in `sessionStorage` |
| `getSession()` | Retrieves user data from `sessionStorage` |
| `isLoggedIn()` | Returns `true` if session exists |
| `logout()` | Clears session and redirects to `/login` |

### Session Storage Structure

```typescript
// Key used in sessionStorage
private readonly SESSION_KEY = 'finvault_user';

// Data stored after login
interface SessionUser {
    userId: number;    // User's database ID
    username: string;  // Display name
    email: string;     // User's email
}

// Example stored value:
sessionStorage.getItem('finvault_user');
// → '{"userId":5,"username":"johndoe","email":"john@mail.com"}'
```

### Why sessionStorage Instead of localStorage?

| Aspect | sessionStorage | localStorage |
|--------|:-------------:|:------------:|
| **Lifespan** | Until tab closes | Forever |
| **Security** | ✅ Auto-logout on close | ⚠️ Persists indefinitely |
| **Scope** | Per-tab | All tabs |
| **FinVault Choice** | ✅ Used | Not used |

> **Security Note:** For a financial app, sessions should NOT persist indefinitely. `sessionStorage` automatically logs users out when they close the browser tab — appropriate for sensitive data.

---

## 🚧 Route Guards

### What is a Route Guard?

Route guards are Angular's mechanism to **control access to routes**. They run BEFORE navigation completes and can allow, block, or redirect.

**File:** `frontend/src/app/guards/auth.guard.ts`

```typescript
export const authGuard: CanActivateFn = () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.isLoggedIn()) {
        return true;                    // ✅ Allow access
    }
    router.navigate(['/login']);        // ❌ Redirect to login
    return false;
};
```

### Protected Routes

**File:** `frontend/src/app/app.routes.ts`

```typescript
export const routes: Routes = [
    { path: '',          redirectTo: 'login', pathMatch: 'full' },
    { path: 'login',     component: LoginComponent },                        // PUBLIC
    { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },  // PROTECTED
    { path: 'simulator', component: SimulatorComponent, canActivate: [authGuard] },  // PROTECTED
    { path: '**',        redirectTo: 'login' }
];
```

### Route Guard Limitations

| What Guards Do | What Guards DON'T Do |
|----------------|----------------------|
| ✅ Block UI navigation | ❌ Block API calls |
| ✅ Redirect to login | ❌ Verify server-side auth |
| ✅ Check sessionStorage | ❌ Validate tokens |

> ⚠️ **Important:** Route guards only protect the **frontend UI**. A technically savvy user can still call `/api/cards` directly using cURL/Postman. True security requires **backend authorization** (JWT validation on every request).

---

## 📝 Registration Flow

### Complete Process

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         REGISTRATION FLOW                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  STEP 1: User fills signup form                                            │
│  ┌─────────────────────────────────┐                                        │
│  │  Username: [johndoe         ]   │                                        │
│  │  Email:    [john@example.com]   │                                        │
│  │  Password: [********        ]   │                                        │
│  │  [Create Account]               │                                        │
│  └─────────────────────────────────┘                                        │
│                  │                                                          │
│                  ▼                                                          │
│  STEP 2: Angular calls AuthService.register()                              │
│  ┌─────────────────────────────────────────────────────────────┐            │
│  │  POST /api/auth/register                                    │            │
│  │  Body: { username: "johndoe",                               │            │
│  │          email: "john@example.com",                         │            │
│  │          password: "MyPassword123" }                        │            │
│  └─────────────────────────────────────────────────────────────┘            │
│                  │                                                          │
│                  ▼                                                          │
│  STEP 3: AuthController.register() receives request                        │
│  ┌─────────────────────────────────────────────────────────────┐            │
│  │  @PostMapping("/register")                                  │            │
│  │  public ResponseEntity<?> register(@RequestBody dto) {      │            │
│  │      Long userId = userService.registerUser(dto);           │            │
│  │      return ResponseEntity.status(201).body(...);           │            │
│  │  }                                                          │            │
│  └─────────────────────────────────────────────────────────────┘            │
│                  │                                                          │
│                  ▼                                                          │
│  STEP 4: UserService.registerUser() does the work                          │
│  ┌─────────────────────────────────────────────────────────────┐            │
│  │  1. Check if email already exists (existsByEmail)           │            │
│  │  2. Create new User entity                                  │            │
│  │  3. Hash password with BCrypt                               │            │
│  │  4. Save to database                                        │            │
│  │  5. Return new user's ID                                    │            │
│  └─────────────────────────────────────────────────────────────┘            │
│                  │                                                          │
│                  ▼                                                          │
│  STEP 5: Response sent back                                                 │
│  ┌─────────────────────────────────────────────────────────────┐            │
│  │  HTTP 201 Created                                           │            │
│  │  { "message": "User registered successfully",               │            │
│  │    "userId": 5 }                                            │            │
│  └─────────────────────────────────────────────────────────────┘            │
│                  │                                                          │
│                  ▼                                                          │
│  STEP 6: Angular auto-logs in the user                                     │
│  ┌─────────────────────────────────────────────────────────────┐            │
│  │  authService.setSession({                                   │            │
│  │      userId: 5,                                             │            │
│  │      username: "johndoe",                                   │            │
│  │      email: "john@example.com"                              │            │
│  │  });                                                        │            │
│  │  router.navigate(['/dashboard']);                           │            │
│  └─────────────────────────────────────────────────────────────┘            │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Error Handling

| Error Scenario | HTTP Status | Error Message |
|----------------|:-----------:|---------------|
| Email already registered | `400 Bad Request` | "Email is already registered: john@mail.com" |
| Missing required field | `400 Bad Request` | (Validation error) |

---

## 🔓 Login Flow

### Complete Process

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            LOGIN FLOW                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  STEP 1: User fills login form                                             │
│  ┌───────────────────────────────┐                                          │
│  │  Email:    [john@example.com] │                                          │
│  │  Password: [********       ]  │                                          │
│  │  [Sign In]                    │                                          │
│  └───────────────────────────────┘                                          │
│                  │                                                          │
│                  ▼                                                          │
│  STEP 2: Angular calls AuthService.login()                                 │
│  ┌─────────────────────────────────────────────────────────────┐            │
│  │  POST /api/auth/login                                       │            │
│  │  Body: { email: "john@example.com",                         │            │
│  │          password: "MyPassword123" }                        │            │
│  └─────────────────────────────────────────────────────────────┘            │
│                  │                                                          │
│                  ▼                                                          │
│  STEP 3: AuthController.login() receives request                           │
│  ┌─────────────────────────────────────────────────────────────┐            │
│  │  return ResponseEntity.ok(userService.loginUser(dto));      │            │
│  └─────────────────────────────────────────────────────────────┘            │
│                  │                                                          │
│                  ▼                                                          │
│  STEP 4: UserService.loginUser() verifies credentials                      │
│  ┌─────────────────────────────────────────────────────────────┐            │
│  │  1. Find user by email (findByEmail)                        │            │
│  │  2. Compare password with BCrypt.matches()                  │            │
│  │  3. If match → return LoginResponseDto                      │            │
│  │  4. If no match → throw "Invalid email or password"         │            │
│  └─────────────────────────────────────────────────────────────┘            │
│                  │                                                          │
│                  ▼                                                          │
│  STEP 5: Success response (HTTP 200)                                       │
│  ┌─────────────────────────────────────────────────────────────┐            │
│  │  { "userId": 5,                                             │            │
│  │    "username": "johndoe",                                   │            │
│  │    "email": "john@example.com",                             │            │
│  │    "message": "Login successful" }                          │            │
│  └─────────────────────────────────────────────────────────────┘            │
│                  │                                                          │
│                  ▼                                                          │
│  STEP 6: AuthService.login() stores session (via tap operator)             │
│  ┌─────────────────────────────────────────────────────────────┐            │
│  │  sessionStorage.setItem('finvault_user', JSON.stringify({   │            │
│  │      userId: 5,                                             │            │
│  │      username: "johndoe",                                   │            │
│  │      email: "john@example.com"                              │            │
│  │  }));                                                       │            │
│  └─────────────────────────────────────────────────────────────┘            │
│                  │                                                          │
│                  ▼                                                          │
│  STEP 7: Navigate to dashboard                                             │
│  ┌─────────────────────────────────────────────────────────────┐            │
│  │  router.navigate(['/dashboard']);                           │            │
│  └─────────────────────────────────────────────────────────────┘            │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Security Note on Error Messages

```java
// We use the SAME error message for both cases:
throw new IllegalArgumentException("Invalid email or password");
```

**Why?** If we said:
- "Email not found" → Hackers know which emails ARE registered
- "Wrong password" → Hackers can enumerate valid accounts

Generic error = **Security through ambiguity**.

---

## 🚪 Logout Flow

### Simple and Stateless

```typescript
// AuthService.logout()
logout(): void {
    sessionStorage.removeItem(this.SESSION_KEY);  // Clear stored data
    this.router.navigate(['/login']);             // Redirect to login
}
```

Since there's no server-side session, logout is purely a frontend operation:
1. Clear `sessionStorage`
2. Redirect to `/login`
3. Done!

> **Note:** If we had JWT, we would also need to handle token invalidation (blacklisting or expiry).

---

## ⚠️ Security Limitations

### Current Gaps

| Vulnerability | Description | Impact | Fix |
|--------------|-------------|--------|-----|
| **No API Authorization** | All endpoints return `permitAll()` | Anyone can call `/api/cards/user/1` | Implement JWT validation |
| **No Token Verification** | Backend doesn't verify session | Frontend session can be faked | JWT with signature verification |
| **No Request Authentication** | API calls don't include credentials | Cannot identify caller | Add `Authorization: Bearer <token>` header |
| **No Session Expiry** | Session lasts until tab closes | Long-lived access | Add JWT expiry + refresh tokens |
| **Client-Only Guard** | Route guard only blocks UI | API still accessible | Backend middleware |

### Production Improvements (Future Sprint)

1. **Add JWT Authentication:**
   ```java
   // Login returns a signed token
   return Map.of("token", jwtService.generateToken(user));
   ```

2. **Protect Endpoints:**
   ```java
   .authorizeHttpRequests(auth -> auth
       .requestMatchers("/api/auth/**").permitAll()
       .anyRequest().authenticated()
   )
   ```

3. **Add JWT Filter:**
   ```java
   .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
   ```

4. **Frontend Interceptor:**
   ```typescript
   // Add token to every request
   request = request.clone({
       setHeaders: { Authorization: `Bearer ${token}` }
   });
   ```

---

## 📁 File Reference

### Backend Files

| File | Layer | Purpose |
|------|:-----:|---------|
| `config/SecurityConfig.java` | Config | CORS, BCrypt, SecurityFilterChain |
| `controller/AuthController.java` | Controller | `/api/auth/register`, `/api/auth/login` |
| `service/UserService.java` | Service | Registration & login business logic |
| `repository/UserRepository.java` | Repository | Database queries (findByEmail, existsByEmail) |
| `entity/User.java` | Entity | User table mapping (id, email, passwordHash) |
| `dto/LoginRequestDto.java` | DTO | Incoming login credentials |
| `dto/LoginResponseDto.java` | DTO | Outgoing user info after login |
| `dto/UserRegistrationDto.java` | DTO | Incoming registration data |

### Frontend Files

| File | Type | Purpose |
|------|:----:|---------|
| `services/auth.service.ts` | Service | API calls, session management |
| `guards/auth.guard.ts` | Guard | Route protection (CanActivateFn) |
| `login/login.component.ts` | Component | Login & signup form logic |
| `login/login.component.html` | Template | Login & signup form UI |
| `app.routes.ts` | Routing | Route definitions + guard assignment |

---

## ❓ Interview Q&A

### Q1: Why use BCrypt instead of SHA-256?
> SHA-256 is fast — too fast! Hackers can hash billions of passwords per second. BCrypt is slow by design (cost factor), making brute-force attacks impractical. It also includes salt automatically.

### Q2: What's the difference between authentication and authorization?
> **Authentication** = "Who are you?" (Login verifies identity)  
> **Authorization** = "What can you do?" (Permissions check)  
> FinVault currently has authentication but no real authorization (all endpoints are public).

### Q3: Why disable CSRF for REST APIs?
> CSRF protection prevents malicious websites from making authenticated requests using your cookies. But REST APIs are stateless — they don't use session cookies. Authentication happens via tokens in headers, which aren't sent automatically by browsers.

### Q4: What happens if someone opens DevTools and modifies sessionStorage?
> They could set a fake `userId`. Currently, the backend would accept their API calls because there's no server-side verification. This is why **JWT is needed** — tokens are cryptographically signed and tamper-proof.

### Q5: Why use sessionStorage instead of localStorage?
> `sessionStorage` auto-clears when the tab closes — appropriate for financial apps where sessions shouldn't persist indefinitely. `localStorage` would keep users "logged in" forever (security risk without token expiry).

### Q6: How would you add JWT to this project?
> 1. Add `jjwt` dependency to `pom.xml`
> 2. Create `JwtService` with `generateToken()` and `validateToken()`
> 3. Modify login to return token instead of user data
> 4. Create `JwtAuthenticationFilter` to intercept requests
> 5. Add filter to SecurityFilterChain
> 6. Create Angular HTTP interceptor to attach token to headers
> 7. Update SecurityConfig to require authentication

---

<p align="center">
  <strong>FinVault Authentication Documentation v1.0</strong><br/>
  <em>Session-based authentication with BCrypt password hashing</em>
</p>

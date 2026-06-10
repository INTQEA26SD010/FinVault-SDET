// ─────────────────────────────────────────────────────────────────────────────
// AUTH SERVICE — Central service for all authentication operations in FinVault
// ─────────────────────────────────────────────────────────────────────────────
//
// WHAT IS THIS FILE?
// This is an Angular Service that handles:
//   1. User registration (calling POST /api/auth/register)
//   2. User login (calling POST /api/auth/login)
//   3. Session management (storing/retrieving user data in sessionStorage)
//   4. Logout (clearing session and redirecting)
//
// WHY A SERVICE?
// Services in Angular are used to share logic across multiple components.
// Instead of each component making HTTP calls directly, they all use this
// centralized AuthService. This follows the DRY principle (Don't Repeat Yourself).
//
// HOW IT FITS IN THE APP:
//   LoginComponent → AuthService → HTTP → Spring Boot Backend → MySQL
//   DashboardComponent → AuthService.getSession() → sessionStorage
//   AuthGuard → AuthService.isLoggedIn() → sessionStorage
//
// ─────────────────────────────────────────────────────────────────────────────

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

// ─────────────────────────────────────────────────────────────────────────────
// INTERFACES — TypeScript types that define the shape of data
// ─────────────────────────────────────────────────────────────────────────────
// Interfaces enforce type safety. If you try to pass wrong data, TypeScript
// will show an error BEFORE you even run the app. This catches bugs early!

/**
 * Data sent TO the backend when a user creates a new account.
 * 
 * Maps to: backend/dto/UserRegistrationDto.java
 */
export interface UserRegistrationRequest {
  username: string;   // Display name (e.g., "johndoe")
  email: string;      // Login identifier (e.g., "john@mail.com")
  password: string;   // Raw password — backend will hash it with BCrypt
}

/**
 * Data received FROM the backend after successful registration.
 * 
 * Maps to: AuthController returns Map.of("message", "...", "userId", id)
 */
export interface RegistrationResponse {
  message: string;    // "User registered successfully"
  userId: number;     // The new user's database ID (auto-generated)
}

/**
 * Data sent TO the backend when a user logs in.
 * 
 * Maps to: backend/dto/LoginRequestDto.java
 */
export interface LoginRequest {
  email: string;      // The email they registered with
  password: string;   // The password to verify (backend compares with BCrypt)
}

/**
 * Data received FROM the backend after successful login.
 * 
 * Maps to: backend/dto/LoginResponseDto.java
 */
export interface LoginResponse {
  userId: number;     // User's database ID — used to fetch their cards
  username: string;   // Display name for the dashboard greeting
  email: string;      // Email shown in the navbar
  message: string;    // "Login successful"
}

/**
 * Data stored in sessionStorage to track the logged-in user.
 * 
 * This is a subset of LoginResponse — we don't need the message after login.
 */
export interface SessionUser {
  userId: number;     // Used for API calls like GET /api/cards/user/{userId}
  username: string;   // Displayed on dashboard: "Good morning, {username}"
  email: string;      // Shown in the navbar profile area
}

// ─────────────────────────────────────────────────────────────────────────────
// AUTH SERVICE CLASS
// ─────────────────────────────────────────────────────────────────────────────

@Injectable({
  providedIn: 'root'
  // ↑ This tells Angular to create ONE instance of AuthService for the entire app.
  //   This is called a "singleton". Every component that injects AuthService
  //   gets the SAME instance, so session data is consistent everywhere.
})
export class AuthService {

  // ─── CONFIGURATION ─────────────────────────────────────────────────────────
  
  /**
   * Base URL for all auth-related API calls.
   * Centralized via `environment.apiUrl`.
   */
  private readonly apiUrl = `${environment.apiUrl}/auth`;
  
  /**
   * Key used to store/retrieve session data in browser's sessionStorage.
   * sessionStorage is cleared when the browser tab closes — appropriate for
   * a financial app where sessions shouldn't persist indefinitely.
   */
  private readonly SESSION_KEY = 'finvault_user';

  // ─── DEPENDENCY INJECTION ──────────────────────────────────────────────────
  
  /**
   * Angular's Dependency Injection (DI) provides these services automatically.
   * 
   * @param http - HttpClient for making HTTP requests to the backend
   * @param router - Router for programmatic navigation (e.g., redirect to /login)
   */
  constructor(private http: HttpClient, private router: Router) {}

  // ─────────────────────────────────────────────────────────────────────────
  // REGISTER — Create a new FinVault account
  // ─────────────────────────────────────────────────────────────────────────
  //
  // FLOW:
  //   1. User fills signup form in LoginComponent
  //   2. LoginComponent calls authService.register(payload)
  //   3. This method sends POST request to /api/auth/register
  //   4. Backend hashes password, saves to DB, returns userId
  //   5. LoginComponent receives response and auto-logs in the user
  //
  // NOTE: This method returns an Observable. The actual HTTP request is NOT
  // made until someone subscribes to it (LoginComponent does .subscribe()).
  //
  register(payload: UserRegistrationRequest): Observable<RegistrationResponse> {
    return this.http.post<RegistrationResponse>(`${this.apiUrl}/register`, payload);
    // POST /api/auth/register
    // Body: { username: "...", email: "...", password: "..." }
    // Response: { message: "...", userId: 5 }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // LOGIN — Authenticate an existing user
  // ─────────────────────────────────────────────────────────────────────────
  //
  // FLOW:
  //   1. User enters email + password in login form
  //   2. LoginComponent calls authService.login(payload)
  //   3. This method sends POST request to /api/auth/login
  //   4. Backend verifies credentials with BCrypt
  //   5. If valid: returns user info (userId, username, email)
  //   6. tap() operator stores this data in sessionStorage
  //   7. LoginComponent navigates to /dashboard
  //
  // RxJS OPERATORS EXPLAINED:
  //   .pipe()  - Chains operators to transform the Observable stream
  //   tap()    - "Side effect" operator — does something WITHOUT changing the data
  //              Perfect for storing session data while passing response through
  //
  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, payload).pipe(
      tap(res => {
        // This runs AFTER a successful HTTP response, but BEFORE the subscriber
        // receives the data. We use this moment to save the user session.
        const user: SessionUser = { 
          userId: res.userId, 
          username: res.username, 
          email: res.email 
        };
        sessionStorage.setItem(this.SESSION_KEY, JSON.stringify(user));
        // Now sessionStorage contains: {"userId":5,"username":"johndoe","email":"john@mail.com"}
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────────────
  // SET SESSION — Manually store session data
  // ─────────────────────────────────────────────────────────────────────────
  //
  // WHEN IS THIS USED?
  // After registration, we want to auto-log the user in immediately.
  // But register() doesn't return full user info (only userId + message).
  // So LoginComponent constructs a SessionUser from the form data and
  // calls this method to save it.
  //
  // WHY sessionStorage INSTEAD OF localStorage?
  //   sessionStorage - Cleared when tab closes (secure for financial apps)
  //   localStorage   - Persists forever (less secure without token expiry)
  //
  setSession(user: SessionUser): void {
    sessionStorage.setItem(this.SESSION_KEY, JSON.stringify(user));
  }

  // ─────────────────────────────────────────────────────────────────────────
  // GET SESSION — Retrieve current logged-in user data
  // ─────────────────────────────────────────────────────────────────────────
  //
  // USED BY:
  //   - DashboardComponent.ngOnInit() — to display username, make API calls
  //   - isLoggedIn() — to check if user is authenticated
  //
  // RETURNS:
  //   SessionUser object if logged in
  //   null if not logged in (or if stored data is corrupted)
  //
  getSession(): SessionUser | null {
    const raw = sessionStorage.getItem(this.SESSION_KEY);
    if (!raw) return null;
    
    // JSON.parse can throw if the data is corrupted/invalid
    // We catch this and return null to handle gracefully
    try { 
      return JSON.parse(raw); 
    } catch { 
      return null; 
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // IS LOGGED IN — Check authentication status
  // ─────────────────────────────────────────────────────────────────────────
  //
  // USED BY:
  //   - authGuard — to protect routes like /dashboard and /simulator
  //   - LoginComponent constructor — to redirect already-logged-in users
  //
  // HOW IT WORKS:
  //   If getSession() returns a user object → user is logged in → return true
  //   If getSession() returns null → no session → return false
  //
  // SECURITY NOTE:
  //   This only checks CLIENT-SIDE session. A clever user could manually
  //   add fake data to sessionStorage. True security requires JWT tokens
  //   that the backend validates on every request.
  //
  isLoggedIn(): boolean {
    return this.getSession() !== null;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // LOGOUT — End the user's session
  // ─────────────────────────────────────────────────────────────────────────
  //
  // WHAT IT DOES:
  //   1. Removes session data from sessionStorage
  //   2. Redirects user to /login page
  //
  // CALLED FROM:
  //   - DashboardComponent (logout button in navbar)
  //
  // NOTE: Since we don't have server-side sessions (stateless API),
  //   logout is purely a client-side operation. With JWT, we would also
  //   need to invalidate/blacklist the token on the server.
  //
  logout(): void {
    sessionStorage.removeItem(this.SESSION_KEY);
    this.router.navigate(['/login']);
  }
}

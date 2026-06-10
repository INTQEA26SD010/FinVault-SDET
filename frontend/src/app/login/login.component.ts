// ─────────────────────────────────────────────────────────────────────────────
// LOGIN COMPONENT — Handles user authentication (Login & Registration forms)
// ─────────────────────────────────────────────────────────────────────────────
//
// WHAT DOES THIS COMPONENT DO?
// This is the main authentication UI for FinVault. It provides:
//   1. A LOGIN form (email + password) for existing users
//   2. A SIGNUP form (username + email + password) for new users
//   3. Tab switching between the two forms
//   4. Form validation with error messages
//   5. Loading states during API calls
//
// UI LAYOUT:
//   ┌─────────────────────────────────────────────────────────────┐
//   │  LEFT PANEL (Desktop only)  │  RIGHT PANEL                 │
//   │  - FinVault branding        │  - Tab switcher (Login/Signup)│
//   │  - Feature list             │  - Form inputs               │
//   │  - Decorative card mock     │  - Submit button             │
//   │                             │  - Error/success messages    │
//   └─────────────────────────────────────────────────────────────┘
//
// FILES IN THIS COMPONENT:
//   login.component.ts   ← This file (logic)
//   login.component.html ← Template (forms, buttons)
//   login.component.css  ← Styles (two-panel layout)
//
// ANGULAR CONCEPTS USED:
//   - Standalone Component (no NgModule needed)
//   - Two-Way Binding [(ngModel)] for form inputs
//   - Template-Driven Forms with validation
//   - Observables (subscribe to HTTP responses)
//   - Dependency Injection (AuthService, Router)
//
// ─────────────────────────────────────────────────────────────────────────────

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, UserRegistrationRequest, LoginRequest } from '../services/auth.service';

// ─────────────────────────────────────────────────────────────────────────────
// COMPONENT DECORATOR — Metadata that tells Angular about this component
// ─────────────────────────────────────────────────────────────────────────────

@Component({
  selector: 'app-login',        // Used as <app-login></app-login> in templates (not used in routing)
  standalone: true,              // Modern Angular: no need for NgModule
  imports: [CommonModule, FormsModule],  // Required modules for *ngIf, ngModel, etc.
  templateUrl: './login.component.html', // External template file
  styleUrl: './login.component.css'      // External styles file
})
export class LoginComponent {

  // ─── UI STATE ──────────────────────────────────────────────────────────────
  
  /**
   * Tracks which tab is currently active: 'login' or 'signup'.
   * The template uses this to show/hide the correct form.
   */
  activeTab: 'login' | 'signup' = 'login';

  // ─── FORM DATA (Two-Way Binding with ngModel) ──────────────────────────────
  
  /**
   * Data bound to the LOGIN form inputs.
   * When user types in email field → loginData.email updates automatically.
   * When user types in password field → loginData.password updates automatically.
   * This is called "two-way binding" and uses [(ngModel)] syntax in the template.
   */
  loginData: LoginRequest = { email: '', password: '' };

  /**
   * Data bound to the SIGNUP form inputs.
   * Same two-way binding as loginData, but with username field added.
   */
  signupData: UserRegistrationRequest = { username: '', email: '', password: '' };

  // ─── LOADING & FEEDBACK ────────────────────────────────────────────────────
  
  /**
   * True while an API call is in progress.
   * Used to:
   *   - Show loading spinner on the button
   *   - Disable the submit button to prevent double-clicks
   */
  isLoading = false;
  
  /**
   * Success message to display (e.g., "Registration successful!").
   * Shown in a green alert box above the form.
   */
  successMessage = '';
  
  /**
   * Error message to display (e.g., "Invalid email or password").
   * Shown in a red alert box above the form.
   */
  errorMessage = '';

  // ─── DEPENDENCY INJECTION ──────────────────────────────────────────────────
  
  /**
   * Angular's DI system passes these services to the constructor automatically.
   * 
   * @param authService - For login, register, and session management
   * @param router - For navigating to /dashboard after successful auth
   * 
   * WHY private?
   * TypeScript shorthand: `private authService: AuthService` in the constructor
   * automatically creates a class property AND assigns the injected value.
   * Equivalent to:
   *   private authService: AuthService;
   *   constructor(authService: AuthService) { this.authService = authService; }
   */
  constructor(private authService: AuthService, private router: Router) {
    // AUTO-REDIRECT: If user is already logged in, send them to dashboard
    // This prevents logged-in users from seeing the login page unnecessarily
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/dashboard']);
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // TAB SWITCHING — Toggle between Login and Signup forms
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Called when user clicks "Sign In" or "Create Account" tab.
   * Switches the active form and clears any previous messages.
   * 
   * @param tab - 'login' or 'signup'
   * 
   * TEMPLATE USAGE:
   *   <button (click)="switchTab('login')">Sign In</button>
   *   <button (click)="switchTab('signup')">Create Account</button>
   */
  switchTab(tab: 'login' | 'signup'): void {
    this.activeTab = tab;
    // Clear old messages when switching tabs (fresh start)
    this.successMessage = '';
    this.errorMessage = '';
  }

  // ─────────────────────────────────────────────────────────────────────────
  // LOGIN — Handle login form submission
  // ─────────────────────────────────────────────────────────────────────────
  //
  // FLOW:
  //   1. User fills email + password and clicks "Sign In"
  //   2. Template calls (ngSubmit)="onLogin()"
  //   3. We set isLoading = true (shows spinner, disables button)
  //   4. Call authService.login() which:
  //      - Sends POST /api/auth/login
  //      - If successful, stores session in sessionStorage
  //   5. On success: navigate to /dashboard
  //   6. On error: display error message
  //
  // RxJS SUBSCRIPTION PATTERN:
  //   .subscribe({
  //     next: (response) => { /* success handler */ },
  //     error: (err) => { /* error handler */ }
  //   })
  //
  // ─────────────────────────────────────────────────────────────────────────

  onLogin(): void {
    // Show loading state
    this.isLoading = true;
    this.errorMessage = '';

    // Call the backend via AuthService
    this.authService.login(this.loginData).subscribe({
      // ✅ SUCCESS: Login worked
      next: () => {
        this.isLoading = false;
        // Session is already stored by authService.login() (via tap operator)
        // Now we can navigate to the dashboard
        this.router.navigate(['/dashboard']);
      },
      // ❌ ERROR: Login failed (wrong credentials, server error, etc.)
      error: (err) => {
        this.isLoading = false;
        // Extract error message from response, or use fallback
        // Backend returns: { "error": "Invalid email or password" }
        this.errorMessage = err.error?.error || 'Login failed. Please check your credentials.';
      }
    });
  }

  // ─────────────────────────────────────────────────────────────────────────
  // SIGNUP — Handle registration form submission
  // ─────────────────────────────────────────────────────────────────────────
  //
  // FLOW:
  //   1. User fills username + email + password and clicks "Create Account"
  //   2. Template calls (ngSubmit)="onSignup()"
  //   3. Call authService.register() which:
  //      - Sends POST /api/auth/register
  //      - Backend hashes password with BCrypt, saves to DB
  //      - Returns { message: "...", userId: 5 }
  //   4. On success:
  //      - Call setSession() to auto-log the user in (no need to re-login)
  //      - Navigate to /dashboard
  //   5. On error: display error message (e.g., "Email already exists")
  //
  // WHY setSession() AFTER REGISTER?
  //   register() returns only { message, userId }, not full user info.
  //   We construct SessionUser from the form data we already have:
  //     userId: from response
  //     username: from signupData.username
  //     email: from signupData.email
  //
  // ─────────────────────────────────────────────────────────────────────────

  onSignup(): void {
    // Show loading state
    this.isLoading = true;
    this.successMessage = '';
    this.errorMessage = '';

    // Call the backend via AuthService
    this.authService.register(this.signupData).subscribe({
      // ✅ SUCCESS: Registration worked
      next: (res) => {
        this.isLoading = false;
        
        // Auto-login: create session with the new user's info
        // This saves the user from having to log in again after registering
        this.authService.setSession({
          userId: res.userId,              // From backend response
          username: this.signupData.username, // From form (we typed it)
          email: this.signupData.email     // From form (we typed it)
        });
        
        // Navigate to dashboard
        this.router.navigate(['/dashboard']);
      },
      // ❌ ERROR: Registration failed (duplicate email, etc.)
      error: (err) => {
        this.isLoading = false;
        // Extract error message from response
        // Backend returns: { "error": "Email is already registered: john@mail.com" }
        this.errorMessage = err.error?.error || 'Registration failed. Please try again.';
      }
    });
  }
}

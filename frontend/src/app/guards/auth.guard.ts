// ─────────────────────────────────────────────────────────────────────────────
// AUTH GUARD — Route protection for authenticated-only pages
// ─────────────────────────────────────────────────────────────────────────────
//
// WHAT IS A ROUTE GUARD?
// A Route Guard is Angular's mechanism for controlling access to routes.
// It runs BEFORE navigation completes and can:
//   - Allow the navigation (return true)
//   - Block the navigation (return false)
//   - Redirect to another route (router.navigate())
//
// WHEN DOES THIS GUARD RUN?
// This guard runs whenever a user tries to navigate to a protected route:
//   - Typing /dashboard in the URL bar
//   - Clicking a link to /dashboard
//   - Calling router.navigate(['/dashboard'])
//
// PROTECTED ROUTES IN FINVAULT:
//   /dashboard  → Requires login (shows user's cards)
//   /simulator  → Requires login (simulates transactions)
//
// HOW IT WORKS:
//   1. User tries to access /dashboard
//   2. Angular sees canActivate: [authGuard] on that route
//   3. Angular calls this authGuard function
//   4. We check if user is logged in via AuthService
//   5. If yes → return true (allow access)
//   6. If no → redirect to /login and return false (block access)
//
// SECURITY NOTE:
// Route guards only protect the FRONTEND UI — they prevent navigation.
// They do NOT protect the backend API! A tech-savvy user could still call
// /api/cards directly using cURL or Postman. True security requires
// backend authorization (JWT token validation on every API request).
//
// ─────────────────────────────────────────────────────────────────────────────

import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

// ─────────────────────────────────────────────────────────────────────────────
// AUTH GUARD FUNCTION (Modern Angular Functional Guard)
// ─────────────────────────────────────────────────────────────────────────────
//
// CanActivateFn vs Class-Based Guards:
// In older Angular (< v15), guards were written as classes:
//   @Injectable() export class AuthGuard implements CanActivate {
//     canActivate(): boolean { ... }
//   }
//
// Modern Angular (v15+) prefers functional guards:
//   - Simpler syntax (just a function)
//   - Uses inject() instead of constructor injection
//   - Easier to test and tree-shake
//
// WHY export const INSTEAD OF export function?
// Arrow functions assigned to const are "tree-shakeable" — if not used,
// the bundler removes them from the final build (smaller bundle size).
//
// ─────────────────────────────────────────────────────────────────────────────

export const authGuard: CanActivateFn = () => {
  // inject() is Angular's way to get service instances inside functions.
  // This is equivalent to constructor injection in class-based guards.
  const authService = inject(AuthService);
  const router = inject(Router);

  // Check if user is logged in by looking for session data in sessionStorage
  if (authService.isLoggedIn()) {
    // ✅ User is authenticated — allow them to access the route
    return true;
  }
  
  // ❌ User is NOT logged in — redirect them to the login page
  // The navigation to /dashboard (or /simulator) will be cancelled
  router.navigate(['/login']);
  return false;
};

// ─────────────────────────────────────────────────────────────────────────────
// USAGE IN app.routes.ts:
// ─────────────────────────────────────────────────────────────────────────────
//
//   import { authGuard } from './guards/auth.guard';
//
//   export const routes: Routes = [
//     { path: 'login',     component: LoginComponent },                        // PUBLIC
//     { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },  // PROTECTED
//     { path: 'simulator', component: SimulatorComponent, canActivate: [authGuard] },  // PROTECTED
//   ];
//
// ─────────────────────────────────────────────────────────────────────────────

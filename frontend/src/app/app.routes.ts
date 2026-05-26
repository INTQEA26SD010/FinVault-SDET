// ─────────────────────────────────────────────────────────────────────────────
// APP ROUTES — Central routing configuration for FinVault
// ─────────────────────────────────────────────────────────────────────────────
//
// WHAT IS ROUTING?
// Routing maps URL paths to Angular components. When the browser URL changes,
// Angular loads the corresponding component without a full page reload.
// This is what makes Angular a "Single Page Application" (SPA).
//
// EXAMPLES:
//   URL: http://localhost:4200/login      → Loads LoginComponent
//   URL: http://localhost:4200/dashboard  → Loads DashboardComponent (if logged in)
//   URL: http://localhost:4200/simulator  → Loads SimulatorComponent (if logged in)
//
// PROTECTED vs PUBLIC ROUTES:
//   PUBLIC routes    → Anyone can access (no authGuard)
//   PROTECTED routes → Require login (have canActivate: [authGuard])
//
// FINVAULT ROUTE STRUCTURE:
//   ┌────────────────────────────────────────────────────────────────┐
//   │  PATH           │  COMPONENT         │  PROTECTION            │
//   ├──────────────────────────────────────────────────────────────────
//   │  /               │  → /login          │  Redirect only         │
//   │  /login          │  LoginComponent    │  PUBLIC                │
//   │  /dashboard      │  DashboardComponent│  PROTECTED (authGuard) │
//   │  /simulator      │  SimulatorComponent│  PROTECTED (authGuard) │
//   │  /**             │  → /login          │  Catch-all redirect    │
//   └────────────────────────────────────────────────────────────────┘
//
// ─────────────────────────────────────────────────────────────────────────────

import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { SimulatorComponent } from './simulator/simulator.component';
import { authGuard } from './guards/auth.guard';

// ─────────────────────────────────────────────────────────────────────────────
// ROUTE DEFINITIONS
// ─────────────────────────────────────────────────────────────────────────────
//
// Each route object has:
//   path: string        - The URL path (without leading /)
//   component: Type     - The component to render
//   redirectTo: string  - Redirect to another path (no component needed)
//   pathMatch: string   - 'full' or 'prefix' (for redirects)
//   canActivate: []     - Array of guards to run BEFORE activating route
//
// ─────────────────────────────────────────────────────────────────────────────

export const routes: Routes = [
  
  // ─── ROOT PATH REDIRECT ────────────────────────────────────────────────────
  // When user visits http://localhost:4200/ (no path), redirect to /login
  // pathMatch: 'full' means the ENTIRE URL must be empty to trigger redirect
  { 
    path: '',           
    redirectTo: 'login', 
    pathMatch: 'full' 
  },
  
  // ─── LOGIN PAGE (PUBLIC) ───────────────────────────────────────────────────
  // No authGuard = anyone can access
  // Handles both Login and Registration forms
  { 
    path: 'login',      
    component: LoginComponent 
  },
  
  // ─── DASHBOARD (PROTECTED) ─────────────────────────────────────────────────
  // canActivate: [authGuard] = Must be logged in to access
  // Shows user's virtual cards, account summary, transaction history
  // 
  // WHAT HAPPENS WHEN USER TRIES TO ACCESS /dashboard?
  //   1. Angular sees canActivate: [authGuard]
  //   2. authGuard runs and checks authService.isLoggedIn()
  //   3. If true → user sees DashboardComponent
  //   4. If false → user is redirected to /login
  { 
    path: 'dashboard',  
    component: DashboardComponent,  
    canActivate: [authGuard] 
  },
  
  // ─── TRANSACTION SIMULATOR (PROTECTED) ─────────────────────────────────────
  // Allows users to simulate purchases with their virtual cards
  // Also protected by authGuard (must be logged in)
  { 
    path: 'simulator',  
    component: SimulatorComponent,  
    canActivate: [authGuard] 
  },
  
  // ─── WILDCARD / CATCH-ALL ROUTE ────────────────────────────────────────────
  // ** matches ANY path that wasn't matched by routes above
  // Examples: /xyz, /admin, /foo/bar → all redirect to /login
  // 
  // This is a safety net:
  //   - Handles typos in URLs
  //   - Prevents 404 errors (Angular would show blank page otherwise)
  //   - Must be LAST in the routes array (routes are matched in order)
  { 
    path: '**',         
    redirectTo: 'login' 
  }
];

// ─────────────────────────────────────────────────────────────────────────────
// HOW ROUTES ARE USED IN THE APP:
// ─────────────────────────────────────────────────────────────────────────────
//
// In app.config.ts:
//   import { provideRouter } from '@angular/router';
//   import { routes } from './app.routes';
//   
//   export const appConfig = {
//     providers: [provideRouter(routes)]
//   };
//
// In app.html (or app.component.html):
//   <router-outlet></router-outlet>
//   ↑ This is where the routed component gets rendered
//
// Programmatic Navigation (in components):
//   this.router.navigate(['/dashboard']);  // Go to dashboard
//   this.router.navigate(['/login']);      // Go to login
//
// ─────────────────────────────────────────────────────────────────────────────

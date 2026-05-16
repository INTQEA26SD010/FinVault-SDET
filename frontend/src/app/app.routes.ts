import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { SimulatorComponent } from './simulator/simulator.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '',           redirectTo: 'login', pathMatch: 'full' },
  { path: 'login',      component: LoginComponent },
  { path: 'dashboard',  component: DashboardComponent,  canActivate: [authGuard] },
  { path: 'simulator',  component: SimulatorComponent,  canActivate: [authGuard] },
  { path: '**',         redirectTo: 'login' }
];

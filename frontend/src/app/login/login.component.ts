import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, UserRegistrationRequest, LoginRequest } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {

  activeTab: 'login' | 'signup' = 'login';

  loginData: LoginRequest = { email: '', password: '' };

  signupData: UserRegistrationRequest = { username: '', email: '', password: '' };

  isLoading = false;
  successMessage = '';
  errorMessage = '';

  constructor(private authService: AuthService, private router: Router) {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/dashboard']);
    }
  }

  switchTab(tab: 'login' | 'signup'): void {
    this.activeTab = tab;
    this.successMessage = '';
    this.errorMessage = '';
  }

  onLogin(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.loginData).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.error || 'Login failed. Please check your credentials.';
      }
    });
  }

  onSignup(): void {
    this.isLoading = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.authService.register(this.signupData).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.authService.setSession({
          userId: res.userId,
          username: this.signupData.username,
          email: this.signupData.email
        });
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.error || 'Registration failed. Please try again.';
      }
    });
  }
}

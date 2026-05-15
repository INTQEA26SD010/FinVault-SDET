import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

export interface UserRegistrationRequest {
  username: string;
  email: string;
  password: string;
}

export interface RegistrationResponse {
  message: string;
  userId: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  userId: number;
  username: string;
  email: string;
  message: string;
}

export interface SessionUser {
  userId: number;
  username: string;
  email: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly apiUrl = 'http://localhost:8080/api/auth';
  private readonly SESSION_KEY = 'finvault_user';

  constructor(private http: HttpClient, private router: Router) {}

  register(payload: UserRegistrationRequest): Observable<RegistrationResponse> {
    return this.http.post<RegistrationResponse>(`${this.apiUrl}/register`, payload);
  }

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, payload).pipe(
      tap(res => {
        const user: SessionUser = { userId: res.userId, username: res.username, email: res.email };
        sessionStorage.setItem(this.SESSION_KEY, JSON.stringify(user));
      })
    );
  }

  /** Store session after successful registration + auto-login */
  setSession(user: SessionUser): void {
    sessionStorage.setItem(this.SESSION_KEY, JSON.stringify(user));
  }

  getSession(): SessionUser | null {
    const raw = sessionStorage.getItem(this.SESSION_KEY);
    if (!raw) return null;
    try { return JSON.parse(raw); } catch { return null; }
  }

  isLoggedIn(): boolean {
    return this.getSession() !== null;
  }

  logout(): void {
    sessionStorage.removeItem(this.SESSION_KEY);
    this.router.navigate(['/login']);
  }
}

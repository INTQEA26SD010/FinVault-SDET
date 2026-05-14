import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserRegistrationRequest {
  username: string;
  email: string;
  password: string;
}

export interface RegistrationResponse {
  message: string;
  userId: number;
}

/**
 * AuthService handles all HTTP communication with the FinVault
 * backend authentication endpoints at /api/auth.
 *
 * Uses Angular's HttpClient injected via constructor — compatible
 * with the standalone provideHttpClient() setup in app.config.ts.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  /**
   * Sends a POST /api/auth/register request to create a new user.
   * @param payload username, email, and plaintext password
   * @returns Observable with the API response (message + userId)
   */
  register(payload: UserRegistrationRequest): Observable<RegistrationResponse> {
    return this.http.post<RegistrationResponse>(`${this.apiUrl}/register`, payload);
  }
}

package com.finvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// ─────────────────────────────────────────────────────────────────────────────
// LOGIN REQUEST DTO — Carries the user's login credentials from frontend to backend.
// ─────────────────────────────────────────────────────────────────────────────
//
// WHEN IS THIS USED?
// When a user types their email and password on the Login page and clicks
// "Sign In", the Angular frontend sends this JSON to the backend:
//
//   POST /api/auth/login
//   Body: { "email": "john@mail.com", "password": "MyPassword123" }
//
// Spring automatically deserializes (converts) that JSON into this DTO.
// Then the AuthController passes it to UserService.loginUser() which:
//   1. Finds the user by email in the database
//   2. Compares the password against the stored BCrypt hash
//   3. If they match → returns user details (login success)
//   4. If they don't match → throws an error (login failed)
//
// ─────────────────────────────────────────────────────────────────────────────

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    // The email the user registered with (used as their login identity)
    private String email;

    // The raw password they typed — will be verified against the stored hash
    private String password;
}

package com.finvault.backend.controller;

import com.finvault.backend.dto.LoginRequestDto;
import com.finvault.backend.dto.UserRegistrationDto;
import com.finvault.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// ─────────────────────────────────────────────────────────────────────────────
// AUTH CONTROLLER — Handles user registration and login HTTP requests.
// ─────────────────────────────────────────────────────────────────────────────
//
// WHAT IS A CONTROLLER?
// A Controller is the ENTRY POINT for HTTP requests. When the Angular frontend
// makes an API call (e.g., POST /api/auth/register), Spring routes it to the
// matching method in this controller.
//
// FLOW:
//   Angular Frontend → HTTP Request → THIS CONTROLLER → Service → Repository → DB
//                    ← HTTP Response ←                ← Service ← Repository ←
//
// WHAT DOES THIS CONTROLLER DO?
// 1. POST /api/auth/register  → Create a new user account
// 2. POST /api/auth/login     → Verify credentials and return user info
//
// KEY ANNOTATIONS EXPLAINED:
// @RestController = This class handles HTTP requests AND returns JSON (not HTML)
// @RequestMapping("/api/auth") = All URLs in this class start with /api/auth
// @RequiredArgsConstructor = Auto-generate constructor for dependency injection
//
// ─────────────────────────────────────────────────────────────────────────────

@RestController                   // Tells Spring: "This is a REST API controller"
@RequestMapping("/api/auth")      // Base URL: all endpoints here start with /api/auth
@RequiredArgsConstructor          // Lombok: creates constructor for 'userService' injection
public class AuthController {

    // The service that contains the actual registration/login business logic
    private final UserService userService;

    // ─────────────────────────────────────────────────────────────────────────
    // REGISTER — POST /api/auth/register
    // ─────────────────────────────────────────────────────────────────────────
    //
    // WHAT IT DOES:
    // Creates a new user account in the system.
    //
    // WHAT THE FRONTEND SENDS (JSON body):
    // {
    //   "username": "johndoe",
    //   "email": "john@example.com",
    //   "password": "SecurePass123"
    // }
    //
    // WHAT WE RETURN:
    //   Success → 201 CREATED + { "message": "...", "userId": 5 }
    //   Failure → 400 BAD REQUEST + { "error": "Email is already registered" }
    //
    // HOW IT WORKS:
    // 1. Spring receives the POST request and the JSON body
    // 2. @RequestBody converts the JSON into a UserRegistrationDto object
    // 3. We pass it to userService.registerUser() which does the actual work
    // 4. If successful → return 201 with the new user's ID
    // 5. If email already exists → catch the error and return 400
    //
    @PostMapping("/register")  // Maps to: POST /api/auth/register
    public ResponseEntity<?> register(@RequestBody UserRegistrationDto dto) {
        // @RequestBody = "Take the JSON from the request body and convert it to this DTO"
        // ResponseEntity<?> = A response with any type of body (? = wildcard)
        try {
            // Call the service to register the user (hashes password, saves to DB)
            Long userId = userService.registerUser(dto);

            // Return HTTP 201 CREATED with a JSON body containing the userId
            // Map.of() creates an immutable map: { "message": "...", "userId": 5 }
            return ResponseEntity
                    .status(HttpStatus.CREATED)  // 201 status code
                    .body(Map.of(
                        "message", "User registered successfully",
                        "userId", userId
                    ));
        } catch (IllegalArgumentException ex) {
            // If registration fails (e.g., duplicate email), return 400 with error message
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)  // 400 status code
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN — POST /api/auth/login
    // ─────────────────────────────────────────────────────────────────────────
    //
    // WHAT IT DOES:
    // Verifies the user's email and password, and returns their profile data.
    //
    // WHAT THE FRONTEND SENDS (JSON body):
    // { "email": "john@example.com", "password": "SecurePass123" }
    //
    // WHAT WE RETURN:
    //   Success → 200 OK + { "userId": 5, "username": "johndoe", "email": "...", "message": "..." }
    //   Failure → 401 UNAUTHORIZED + { "error": "Invalid email or password" }
    //
    // NOTE: We return a generic "Invalid email or password" message for BOTH
    // wrong email and wrong password — this prevents hackers from discovering
    // which emails are registered in the system.
    //
    @PostMapping("/login")  // Maps to: POST /api/auth/login
    public ResponseEntity<?> login(@RequestBody LoginRequestDto dto) {
        try {
            // Call the service to verify credentials and get user info
            // If successful, userService returns a LoginResponseDto
            return ResponseEntity.ok(userService.loginUser(dto));
            // .ok() = HTTP 200 with the DTO as JSON body
        } catch (IllegalArgumentException ex) {
            // If login fails, return 401 UNAUTHORIZED
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)  // 401 status code
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}

package com.finvault.backend.controller;

import com.finvault.backend.dto.LoginRequestDto;
import com.finvault.backend.dto.UserRegistrationDto;
import com.finvault.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller handling authentication-related endpoints.
 * Base path: /api/auth
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * POST /api/auth/register
     *
     * Registers a new FinVault user account.
     * Accepts a JSON body with username, email, and password.
     * Returns 201 CREATED and the new user's ID on success.
     * Returns 400 BAD REQUEST if the email is already registered.
     *
     * Example request body:
     * {
     *   "username": "johndoe",
     *   "email": "john@example.com",
     *   "password": "SecurePass123"
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDto dto) {
        try {
            Long userId = userService.registerUser(dto);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                        "message", "User registered successfully",
                        "userId", userId
                    ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto dto) {
        try {
            return ResponseEntity.ok(userService.loginUser(dto));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}

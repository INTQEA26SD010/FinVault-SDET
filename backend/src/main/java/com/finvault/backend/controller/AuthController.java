package com.finvault.backend.controller;

import com.finvault.backend.dto.LoginRequestDto;
import com.finvault.backend.dto.UserRegistrationDto;
import com.finvault.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;



@RestController                   // Tells Spring: "This is a REST API controller"
@RequestMapping("/api/auth")      // Base URL: all endpoints here start with /api/auth
@RequiredArgsConstructor          // Lombok: creates constructor for 'userService' injection
public class AuthController {

    // The service that contains the actual registration/login business logic
    private final UserService userService;

   
    @PostMapping("/register")  // Maps to: POST /api/auth/register
    public ResponseEntity<?> register(@RequestBody UserRegistrationDto dto) {
        
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

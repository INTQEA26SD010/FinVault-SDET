package com.finvault.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// SECURITY CONFIGURATION — Controls authentication, CORS, and password hashing.
// ─────────────────────────────────────────────────────────────────────────────
//
// WHAT IS THIS CLASS?
// This is the security "brain" of the backend. It tells Spring Security:
//   - How to hash passwords (BCrypt)
//   - Which domains can access our API (CORS)
//   - Which endpoints require authentication (currently: none — all are public)
//   - Whether to use CSRF protection (disabled for REST APIs)
//
// KEY CONCEPTS FOR BEGINNERS:
//
// 1. CORS (Cross-Origin Resource Sharing):
//    By default, browsers BLOCK requests from one domain to another.
//    Our Angular frontend runs on http://localhost:4200
//    Our Spring backend runs on http://localhost:8081
//    These are DIFFERENT origins! Without CORS, the browser won't let Angular talk to Spring.
//    This config says: "Allow requests from localhost:4200"
//
// 2. CSRF (Cross-Site Request Forgery):
//    CSRF protection prevents malicious websites from making requests on your behalf.
//    It's important for cookie-based sessions, but our API is STATELESS (no cookies),
//    so we disable it. JWT tokens (added in future) handle security instead.
//
// 3. BCrypt Password Hashing:
//    BCrypt is an algorithm that turns passwords into irreversible hashes.
//    "password123" → "$2a$10$xyz..." (can't be reversed!)
//    Even if a hacker steals the database, they can't read passwords.
//
// ─────────────────────────────────────────────────────────────────────────────

@Configuration       // Tells Spring: "This class contains bean definitions (configuration)"
@EnableWebSecurity   // Activates Spring Security's web security features
public class SecurityConfig {

    // ─────────────────────────────────────────────────────────────────────────
    // PASSWORD ENCODER BEAN — Defines HOW passwords are hashed in the app.
    // ─────────────────────────────────────────────────────────────────────────
    //
    // @Bean = "Register this object in Spring's container so others can @Autowire it"
    //
    // BCryptPasswordEncoder does two things:
    //   1. encode("password") → Creates a hash: "$2a$10$..."
    //   2. matches("password", "$2a$10$...") → Returns true if they match
    //
    // The "strength" (default 10) controls how slow the hashing is.
    // Higher = slower = harder for hackers to brute-force. 10 is standard.
    //
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CORS CONFIGURATION — Which external domains can access our API.
    // ─────────────────────────────────────────────────────────────────────────
    //
    // Without this, the browser would block ALL requests from Angular to our API.
    //
    // We configure:
    //   - Allowed Origins: Only http://localhost:4200 (our Angular dev server)
    //   - Allowed Methods: GET, POST, PUT, DELETE, OPTIONS (all REST methods)
    //   - Allowed Headers: * (any header — needed for Content-Type, Authorization, etc.)
    //   - Allow Credentials: true (allows cookies/auth headers to be sent)
    //   - Pattern "/**": Apply these rules to ALL endpoints
    //
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Which frontend URLs are allowed to call our API
        config.setAllowedOrigins(List.of("http://localhost:4200"));

        // Which HTTP methods are allowed
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Which HTTP headers the frontend can send (* = any)
        config.setAllowedHeaders(List.of("*"));

        // Allow the frontend to include credentials (cookies, Authorization header)
        config.setAllowCredentials(true);

        // Apply this CORS config to ALL URL patterns
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECURITY FILTER CHAIN — The main security rules for our application.
    // ─────────────────────────────────────────────────────────────────────────
    //
    // This defines:
    //   1. Apply CORS (use our config above)
    //   2. Disable CSRF (not needed for stateless REST APIs)
    //   3. Allow ALL requests without authentication (Sprint 1 — public API)
    //
    // In a future sprint, we'll add JWT authentication here:
    //   .authorizeHttpRequests(auth -> auth
    //       .requestMatchers("/api/auth/**").permitAll()  // Login/register = public
    //       .anyRequest().authenticated()                 // Everything else = needs JWT
    //   )
    //
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Apply our CORS configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Disable CSRF — our API is stateless (no session cookies)
            .csrf(csrf -> csrf.disable())

            // Authorization rules: currently ALL endpoints are public (no login needed)
            // This will be tightened with JWT authentication in a future sprint
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()  // Anyone can access any endpoint
            );

        return http.build();
    }
}

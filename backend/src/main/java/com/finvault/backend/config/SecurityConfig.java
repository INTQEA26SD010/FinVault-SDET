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

/**
 * Spring Security baseline configuration for FinVault.
 *
 * Current state (Sprint 1): all endpoints are publicly accessible
 * so the REST API is usable without a login form. JWT authentication
 * will be layered on in a dedicated security sprint.
 *
 * The PasswordEncoder bean is declared here so it can be injected
 * into UserService without circular dependency issues.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Provides BCrypt as the global password hashing strategy.
     * BCrypt is work-factor adaptive — increasing the strength value
     * increases hash time, making brute-force attacks progressively harder.
     * Default strength = 10 (industry standard).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Opens all API endpoints publicly for Sprint 1.
     * CSRF is disabled because our API is stateless (no session cookies).
     * JWT filter will be inserted here in the security sprint.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }
}

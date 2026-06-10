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
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;



@Configuration       // Tells Spring: "This class contains bean definitions (configuration)"
@EnableWebSecurity   // Activates Spring Security's web security features
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:}")
    private String allowedOriginsProp;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Read allowed origins from configuration (comma-separated). If empty,
        // fall back to allowing any origin pattern. Avoid using literal "*"
        // together with credentials.
        List<String> allowedOrigins = Arrays.stream(allowedOriginsProp.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (allowedOrigins.isEmpty()) {
            // Allow any origin pattern — use with caution in production
            config.setAllowedOriginPatterns(List.of("*"));
        } else {
            config.setAllowedOrigins(allowedOrigins);
        }

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

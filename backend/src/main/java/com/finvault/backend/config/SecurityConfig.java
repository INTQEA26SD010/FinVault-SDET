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



@Configuration       // Tells Spring: "This class contains bean definitions (configuration)"
@EnableWebSecurity   // Activates Spring Security's web security features
public class SecurityConfig {

    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    
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

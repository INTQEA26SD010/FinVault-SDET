package com.finvault.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

// ─────────────────────────────────────────────────────────────────────────────
// BACKEND APPLICATION — The main entry point (starting point) of the Spring Boot app.
// ─────────────────────────────────────────────────────────────────────────────
//
// WHAT HAPPENS WHEN YOU RUN THIS CLASS?
// 1. Java calls the main() method
// 2. SpringApplication.run() starts the entire Spring Boot application:
//    - Scans all packages under com.finvault.backend for @Component, @Service,
//      @Controller, @Repository, @Configuration annotated classes
//    - Creates instances of all these classes (beans)
//    - Wires them together (dependency injection)
//    - Starts the embedded Tomcat server on port 8081 (set in application.properties)
//    - Connects to MySQL database
//    - Your REST API is now ready to accept requests!
//
// @SpringBootApplication is a SHORTCUT that combines 3 annotations:
//   @Configuration    = "This class can define @Bean methods"
//   @EnableAutoConfiguration = "Auto-configure based on dependencies in pom.xml"
//   @ComponentScan    = "Scan this package + sub-packages for Spring components"
//
// The "exclude" part:
//   Spring Security normally auto-creates an in-memory user with a random password
//   (you'd see it in the console: "Using generated security password: xyz...").
//   We don't want that — FinVault manages its own users via our UserService.
//   So we exclude that auto-configuration.
//
// ─────────────────────────────────────────────────────────────────────────────

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class BackendApplication {

	// The entry point — Java starts here when you run the application
	public static void main(String[] args) {
		// This single line boots up the ENTIRE application:
		// Tomcat server, database connection, all controllers, services, repos — everything!
		SpringApplication.run(BackendApplication.class, args);
	}

}

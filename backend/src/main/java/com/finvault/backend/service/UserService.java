package com.finvault.backend.service;

import com.finvault.backend.dto.LoginRequestDto;
import com.finvault.backend.dto.LoginResponseDto;
import com.finvault.backend.dto.UserRegistrationDto;
import com.finvault.backend.entity.User;
import com.finvault.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// ─────────────────────────────────────────────────────────────────────────────
// USER SERVICE — Business logic for user registration and login.
// ─────────────────────────────────────────────────────────────────────────────
//
// WHAT IS A SERVICE?
// A Service class contains the "business logic" — the actual rules and steps
// for how things work. It sits BETWEEN the Controller and the Repository:
//
//   Frontend → Controller (receives HTTP request)
//            → SERVICE (applies business rules) ← WE ARE HERE
//            → Repository (talks to database)
//
// WHY SEPARATE SERVICE FROM CONTROLLER?
// 1. Separation of Concerns: Controller handles HTTP, Service handles logic
// 2. Reusability: Multiple controllers can use the same service
// 3. Testability: Easy to unit-test business logic without HTTP stuff
//
// WHAT DOES THIS SERVICE DO?
// 1. registerUser() — Creates a new account (hashes password, saves to DB)
// 2. loginUser()    — Verifies email + password and returns user details
//
// ─────────────────────────────────────────────────────────────────────────────

@Service  // Tells Spring: "This is a Service bean — manage its lifecycle"
@RequiredArgsConstructor  // Lombok: generates constructor for all 'final' fields (dependency injection)
public class UserService {

    // ─── DEPENDENCIES (injected by Spring via constructor) ───────────────────
    //
    // Spring's Dependency Injection (DI) works like this:
    // 1. Spring creates ONE instance of UserRepository and PasswordEncoder
    // 2. When creating UserService, Spring sees these 'final' fields
    // 3. Spring automatically passes the instances via the constructor
    // 4. Now UserService can use them — no "new UserRepository()" needed!

    private final UserRepository userRepository;   // Talks to the "users" table in MySQL
    private final PasswordEncoder passwordEncoder; // BCrypt hasher — turns passwords into hashes

    // ─────────────────────────────────────────────────────────────────────────
    // REGISTER USER — Creates a new FinVault account
    // ─────────────────────────────────────────────────────────────────────────
    //
    // FLOW:
    //   1. Check if email already exists → if yes, throw error (no duplicate accounts!)
    //   2. Create a new User entity
    //   3. Hash the password using BCrypt (NEVER store raw passwords!)
    //   4. Save to database
    //   5. Return the new user's ID
    //
    // CALLED BY: AuthController.register() when POST /api/auth/register is hit
    //
    public Long registerUser(UserRegistrationDto dto) {

        // STEP 1: Check if email is already registered
        // If someone already registered with "john@mail.com", we can't allow another account
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException(
                "Email is already registered: " + dto.getEmail()
            );
        }

        // STEP 2: Create a new User entity (a new row for the database)
        User user = new User();
        user.setUsername(dto.getUsername());    // Set the username from the form
        user.setEmail(dto.getEmail());          // Set the email from the form

        // STEP 3: Hash the password using BCrypt
        // PASSWORD HASHING EXPLAINED:
        //   Raw password: "MyPassword123"
        //   BCrypt hash:  "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
        //
        // This hash is ONE-WAY — you cannot reverse it back to "MyPassword123".
        // During login, BCrypt takes the typed password, hashes it the same way,
        // and compares the two hashes. If they match → correct password!
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        // STEP 4: Save the User to the database
        // .save() does an INSERT INTO users (...) VALUES (...) and returns the saved entity
        // with the auto-generated ID filled in
        User saved = userRepository.save(user);

        // STEP 5: Return the new user's auto-generated database ID
        return saved.getId();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN USER — Authenticates a user by email and password
    // ─────────────────────────────────────────────────────────────────────────
    //
    // FLOW:
    //   1. Find the user by email → if not found, throw error
    //   2. Compare typed password with stored hash → if no match, throw error
    //   3. If both checks pass → return user info (login success!)
    //
    // CALLED BY: AuthController.login() when POST /api/auth/login is hit
    //
    public LoginResponseDto loginUser(LoginRequestDto dto) {

        // STEP 1: Find user by email
        // .orElseThrow() means: if no user found, throw this error
        // NOTE: We use a GENERIC error message "Invalid email or password" for security.
        // We don't say "email not found" because that would tell hackers which emails exist!
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // STEP 2: Verify password
        // passwordEncoder.matches(rawPassword, storedHash) does:
        //   1. Takes the typed password "MyPassword123"
        //   2. Hashes it with the same BCrypt algorithm
        //   3. Compares the new hash with the stored hash
        //   4. Returns true if they match, false otherwise
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // STEP 3: Login successful! Return user details to the frontend
        // The frontend will store this in localStorage to keep the user "logged in"
        return new LoginResponseDto(
                user.getId(),          // userId — used to fetch this user's cards
                user.getUsername(),    // Display name for the dashboard
                user.getEmail(),      // Shown in the navbar
                "Login successful"    // Confirmation message
        );
    }
}

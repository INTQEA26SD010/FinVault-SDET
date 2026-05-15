package com.finvault.backend.service;

import com.finvault.backend.dto.LoginRequestDto;
import com.finvault.backend.dto.LoginResponseDto;
import com.finvault.backend.dto.UserRegistrationDto;
import com.finvault.backend.entity.User;
import com.finvault.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Business logic for user account operations.
 * Sits between the AuthController and UserRepository,
 * ensuring raw passwords are NEVER written to the database.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new FinVault user.
     *
     * Steps:
     * 1. Check if email is already taken — fail fast with a clear message.
     * 2. Hash the plaintext password using BCrypt before persisting.
     * 3. Save the User entity and return the generated database ID.
     *
     * @param dto incoming registration payload from the API layer
     * @return the auto-generated ID of the newly created User row
     * @throws IllegalArgumentException if the email is already registered
     */
    public Long registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException(
                "Email is already registered: " + dto.getEmail()
            );
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        // BCrypt hash — never store plaintext passwords
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        User saved = userRepository.save(user);
        return saved.getId();
    }

    /**
     * Authenticates a user by email and password.
     */
    public LoginResponseDto loginUser(LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return new LoginResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                "Login successful"
        );
    }
}

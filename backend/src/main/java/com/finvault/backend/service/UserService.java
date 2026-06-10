package com.finvault.backend.service;

import com.finvault.backend.dto.LoginRequestDto;
import com.finvault.backend.dto.LoginResponseDto;
import com.finvault.backend.dto.UserRegistrationDto;
import com.finvault.backend.entity.User;
import com.finvault.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



@Service  // Tells Spring: "This is a Service bean — manage its lifecycle"
@RequiredArgsConstructor  // Lombok: generates constructor for all 'final' fields (dependency injection)
public class UserService {

    

    private final UserRepository userRepository;   // Talks to the "users" table in MySQL
    private final PasswordEncoder passwordEncoder; // BCrypt hasher — turns passwords into hashes


    public Long registerUser(UserRegistrationDto dto) {

        
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException(
                "Email is already registered: " + dto.getEmail()
            );
        }

       
        User user = new User();
        user.setUsername(dto.getUsername());    // Set the username from the form
        user.setEmail(dto.getEmail());          // Set the email from the form

        
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        
        User saved = userRepository.save(user);

        
        return saved.getId();
    }

   
    public LoginResponseDto loginUser(LoginRequestDto dto) {

       
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

       
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

       
        return new LoginResponseDto(
                user.getId(),          // userId — used to fetch this user's cards
                user.getUsername(),    // Display name for the dashboard
                user.getEmail(),      // Shown in the navbar
                "Login successful"    // Confirmation message
        );
    }
}

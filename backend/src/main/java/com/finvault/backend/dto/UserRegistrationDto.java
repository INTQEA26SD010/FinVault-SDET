package com.finvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for incoming user registration requests.
 * Never expose the User @Entity directly in the API layer —
 * this DTO prevents over-posting attacks and decouples the
 * API contract from the database schema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {

    private String username;
    private String email;
    private String password;
}

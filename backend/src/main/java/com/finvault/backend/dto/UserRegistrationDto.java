package com.finvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// ─────────────────────────────────────────────────────────────────────────────
// USER REGISTRATION DTO — Data Transfer Object for Sign-Up requests
// ─────────────────────────────────────────────────────────────────────────────
//
// WHAT IS A DTO?
// DTO = Data Transfer Object. It's a simple Java class that carries data
// between layers (Client → Controller → Service). Think of it as an envelope:
// - The client (Angular frontend) puts data into the envelope (JSON body)
// - Spring automatically opens the envelope and fills this DTO's fields
// - The Service layer reads the DTO to know what the user wants to do
//
// WHY NOT USE THE ENTITY DIRECTLY?
// Security! The User entity has fields like "id", "passwordHash", "createdAt"
// that the client should NEVER be able to set. If we accepted a User object
// directly, a hacker could send: { "id": 1, "passwordHash": "evil" }
// and overwrite existing data! DTOs prevent this by only exposing safe fields.
//
// WHEN IS THIS USED?
// When a new user fills in the registration form on the Angular frontend and
// clicks "Create Account", this is the JSON structure sent to the backend:
//
//   POST /api/auth/register
//   Body: { "username": "john", "email": "john@mail.com", "password": "Pass123" }
//
// Spring automatically converts that JSON into this UserRegistrationDto object.
//
// ─────────────────────────────────────────────────────────────────────────────

@Data             // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor    // Empty constructor: new UserRegistrationDto()
@AllArgsConstructor   // Full constructor: new UserRegistrationDto("john", "john@mail.com", "pass")
public class UserRegistrationDto {

    // The display name the user chose (e.g., "johndoe")
    private String username;

    // The user's email — will be used for login
    private String email;

    // The user's RAW password (e.g., "MyPassword123")
    // NOTE: This is the PLAIN TEXT password from the form.
    // It will be hashed (encrypted) by BCrypt in the UserService before storing.
    // We NEVER store this raw value in the database.
    private String password;
}

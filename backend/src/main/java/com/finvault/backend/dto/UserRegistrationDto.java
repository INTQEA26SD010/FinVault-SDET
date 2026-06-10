package com.finvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data             // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor    // Empty constructor: new UserRegistrationDto()
@AllArgsConstructor   // Full constructor: new UserRegistrationDto("john", "john@mail.com", "pass")
public class UserRegistrationDto {

    // The display name the user chose (e.g., "johndoe")
    private String username;

    // The user's email — will be used for login
    private String email;

    // The user's RAW password (e.g., "MyPassword123")
    private String password;
}

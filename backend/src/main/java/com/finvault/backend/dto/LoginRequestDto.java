package com.finvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    // The email the user registered with (used as their login identity)
    private String email;

    // The raw password they typed — will be verified against the stored hash
    private String password;
}

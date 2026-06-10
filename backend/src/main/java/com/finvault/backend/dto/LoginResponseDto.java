package com.finvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    // The user's database ID — frontend uses this to call /api/cards/user/{userId}
    private Long userId;

    // The user's display name — shown on the dashboard
    private String username;

    // The user's email — displayed in the navbar
    private String email;

    // A human-readable message like "Login successful"
    private String message;
}

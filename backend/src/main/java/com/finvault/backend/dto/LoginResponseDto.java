package com.finvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// ─────────────────────────────────────────────────────────────────────────────
// LOGIN RESPONSE DTO — Data sent BACK to the frontend after a successful login.
// ─────────────────────────────────────────────────────────────────────────────
//
// WHAT HAPPENS AFTER LOGIN?
// After the user's email + password are verified, we need to tell the frontend:
// "Login worked! Here's your user info so you can display it on the dashboard."
//
// This DTO contains the data the Angular frontend needs to:
//   - Store the userId (used to fetch that user's cards and transactions)
//   - Display the username on the dashboard greeting ("Good morning, John 👋")
//   - Display the email in the navbar
//   - Show a success message
//
// The Angular frontend stores this data in localStorage so it persists
// even if the user refreshes the page.
//
// EXAMPLE RESPONSE (sent as JSON):
// {
//   "userId": 5,
//   "username": "johndoe",
//   "email": "john@mail.com",
//   "message": "Login successful"
// }
//
// ─────────────────────────────────────────────────────────────────────────────

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

package com.finvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// ─────────────────────────────────────────────────────────────────────────────
// VIRTUAL CARD RESPONSE DTO — Data sent BACK to the frontend about a card.
// ─────────────────────────────────────────────────────────────────────────────
//
// WHAT IS THIS FOR?
// When the frontend asks "show me this user's cards", we don't want to send
// the raw VirtualCard entity because it contains:
//   - The full User object (password hash, etc.) — SECURITY RISK!
//   - JPA metadata and relationships — unnecessary data
//
// Instead, we carefully pick ONLY the fields the frontend needs and put them
// in this DTO. This is what the Angular frontend receives as JSON.
//
// EXAMPLE RESPONSE:
// {
//   "id": 3,
//   "cardNumber": "4532789012345678",
//   "cvv": "291",
//   "dailyLimit": 500.00,
//   "balance": 120.50,
//   "status": "ACTIVE",
//   "vendorName": "Amazon"
// }
//
// NOTE: The cvv IS included here because the card owner should see it.
// In a production app, you might hide it after initial creation.
//
// ─────────────────────────────────────────────────────────────────────────────

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VirtualCardResponseDto {

    // The card's database ID (used for toggle/delete operations)
    private Long id;

    // The 16-digit card number (e.g., "4532789012345678")
    // Frontend displays last 4 digits: "•••• •••• •••• 5678"
    private String cardNumber;

    // The 3-digit security code (e.g., "291")
    private String cvv;

    // Maximum spend allowed per day (e.g., 500.00)
    private BigDecimal dailyLimit;

    // How much has been spent today (e.g., 120.50 out of 500.00)
    private BigDecimal balance;

    // Current card state: "ACTIVE" or "FROZEN"
    private String status;

    // What this card is for (e.g., "Amazon", "Netflix")
    private String vendorName;
}

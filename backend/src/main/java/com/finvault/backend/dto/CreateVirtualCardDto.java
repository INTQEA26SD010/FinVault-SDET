package com.finvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// ─────────────────────────────────────────────────────────────────────────────
// CREATE VIRTUAL CARD DTO — Input data for creating a new virtual card.
// ─────────────────────────────────────────────────────────────────────────────
//
// WHEN IS THIS USED?
// When a user clicks "+ Generate Card" on the dashboard, the Angular frontend
// sends a POST request with this data:
//
//   POST /api/cards
//   Body: { "userId": 1, "dailyLimit": 500.00, "vendorName": "Amazon" }
//
// The backend then:
//   1. Finds the User with the given userId
//   2. Creates a new VirtualCard with a random 16-digit number, CVV, expiry date
//   3. Sets the dailyLimit and vendorName from this DTO
//   4. Saves it to the database
//   5. Returns the created card details to the frontend
//
// WHY ONLY 3 FIELDS?
// Everything else (cardNumber, CVV, expiryDate, status, balance) is auto-generated
// by the backend. The user only needs to specify:
//   - WHO owns the card (userId)
//   - HOW MUCH they can spend per day (dailyLimit)
//   - WHAT it's for (vendorName — like "Netflix", "Amazon")
//
// ─────────────────────────────────────────────────────────────────────────────

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVirtualCardDto {

    // Which user is creating this card — maps to the User.id in the database
    private Long userId;

    // Maximum amount this card can spend per day (e.g., 500.00 means $500/day)
    private BigDecimal dailyLimit;

    // Human-readable label for the card's purpose (e.g., "Amazon", "Netflix", "Groceries")
    // Displayed on the card in the dashboard UI
    private String vendorName;
}

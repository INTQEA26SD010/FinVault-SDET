package com.finvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// ─────────────────────────────────────────────────────────────────────────────
// TRANSACTION REQUEST DTO — Input data for simulating a transaction (purchase).
// ─────────────────────────────────────────────────────────────────────────────
//
// WHEN IS THIS USED?
// When a user uses the "Simulator" page to test a purchase on a card:
//
//   POST /api/transactions
//   Body: { "cardId": 3, "amount": 49.99, "merchantName": "Netflix" }
//
// This tells the backend: "Try to charge $49.99 on card #3 at Netflix"
//
// The backend then checks:
//   - Does card #3 exist? If not → error
//   - Is card #3 ACTIVE? (frozen cards can't be used)
//   - Would this charge exceed the daily limit?
//     - If card's balance + amount ≤ dailyLimit → SUCCESS (money deducted)
//     - If card's balance + amount > dailyLimit → DECLINED (no charge)
//
// Either way, the transaction is SAVED to the database for history.
//
// ─────────────────────────────────────────────────────────────────────────────

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDto {

    // Which virtual card to charge (the card's database ID)
    private Long cardId;

    // How much money to spend (must be > 0)
    // Example: 49.99 means $49.99
    private BigDecimal amount;

    // The name of the store/service (e.g., "Netflix", "Amazon", "Uber")
    private String merchantName;
}

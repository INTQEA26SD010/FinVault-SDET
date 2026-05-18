package com.finvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// ─────────────────────────────────────────────────────────────────────────────
// TRANSACTION RESPONSE DTO — Data sent BACK to the frontend after a transaction.
// ─────────────────────────────────────────────────────────────────────────────
//
// WHAT IS THIS FOR?
// After the backend processes a transaction (approve or decline), it sends
// this DTO back as JSON so the frontend can display the result.
//
// EXAMPLE RESPONSE:
// {
//   "id": 12,
//   "cardId": 3,
//   "amount": 49.99,
//   "merchantName": "Netflix",
//   "timestamp": "2026-05-18T14:30:22",
//   "status": "SUCCESS"       ← or "DECLINED"
// }
//
// The Angular frontend uses this to:
//   - Show a success/error alert in the Simulator
//   - Add the transaction to the transaction history table
//   - Update the card's displayed balance
//
// ─────────────────────────────────────────────────────────────────────────────

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDto {

    // The transaction's database ID (unique identifier for this record)
    private Long id;

    // Which card was charged (ID, not the full card number — for security)
    private Long cardId;

    // How much was charged (e.g., 49.99)
    private BigDecimal amount;

    // Where the money was spent (e.g., "Netflix")
    private String merchantName;

    // When the transaction happened (e.g., "2026-05-18T14:30:22")
    private LocalDateTime timestamp;

    // The outcome: "SUCCESS" (money charged) or "DECLINED" (over daily limit)
    private String status;
}

package com.finvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;



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

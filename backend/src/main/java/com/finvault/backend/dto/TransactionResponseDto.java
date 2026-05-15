package com.finvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Outbound DTO returned after processing a transaction.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDto {

    private Long id;
    private Long cardId;
    private BigDecimal amount;
    private String merchantName;
    private LocalDateTime timestamp;
    private String status;
}

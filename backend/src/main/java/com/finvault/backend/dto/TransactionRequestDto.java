package com.finvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Inbound DTO for submitting a transaction request.
 * The caller provides the card to charge, the amount, and the merchant name.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDto {

    /** ID of the VirtualCard to charge. */
    private Long cardId;

    /** Amount to spend (must be > 0). */
    private BigDecimal amount;

    /** Name of the merchant where the spend occurs. */
    private String merchantName;
}

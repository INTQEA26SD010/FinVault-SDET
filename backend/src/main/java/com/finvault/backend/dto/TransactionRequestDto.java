package com.finvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;



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

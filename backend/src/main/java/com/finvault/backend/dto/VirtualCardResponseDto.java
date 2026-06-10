package com.finvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


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

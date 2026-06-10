package com.finvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;



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

package com.finvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for outbound virtual card data returned by the REST API.
 * Deliberately omits sensitive fields (cvv, userId FK) that must
 * never be serialised into a public API response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VirtualCardResponseDto {

    private Long id;
    private String cardNumber;
    private String cvv;
    private BigDecimal dailyLimit;
    private BigDecimal balance;
    private String status;
    private String vendorName;
}

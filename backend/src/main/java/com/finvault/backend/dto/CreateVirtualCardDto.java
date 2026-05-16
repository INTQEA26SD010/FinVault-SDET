package com.finvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for inbound virtual card creation requests.
 * Accepts the owning user's ID and the desired daily spending limit.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVirtualCardDto {

    private Long userId;
    private BigDecimal dailyLimit;
    /** Human-readable vendor or purpose label required at card-creation time. */
    private String vendorName;
}

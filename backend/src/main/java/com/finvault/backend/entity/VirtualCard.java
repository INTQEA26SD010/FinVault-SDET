package com.finvault.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity mapping to the `virtual_cards` table in finvault_db.
 * Represents a virtual smart card issued to a FinVault user,
 * acting as a per-user spending firewall with a configurable daily limit.
 */
@Entity
@Table(name = "virtual_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VirtualCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many cards belong to one user.
     * ForeignKey column `user_id` is managed here.
     * LAZY loading avoids fetching the full User object
     * every time a card is retrieved.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "card_number", nullable = false, unique = true, length = 16)
    private String cardNumber;

    /**
     * Stored as the last valid date of the card (e.g., 2028-12-31).
     * Mapped to SQL DATE type via LocalDate.
     */
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "cvv", nullable = false, length = 3)
    private String cvv;

    /**
     * Maximum spend allowed per day in the base currency.
     * BigDecimal ensures no floating-point precision loss for monetary values.
     */
    @Column(name = "daily_limit", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyLimit = BigDecimal.ZERO;

    /**
     * Lifecycle state of the card.
     * Mapped to the MySQL ENUM column using @Enumerated(STRING)
     * so the string name (not ordinal) is stored — safe for DB refactoring.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CardStatus status = CardStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Mirrors the ENUM constraint defined in the `virtual_cards` SQL table.
     */
    public enum CardStatus {
        ACTIVE,
        FROZEN,
        EXPIRED,
        CANCELLED
    }
}

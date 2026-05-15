package com.finvault.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity mapping to the `transactions` table in finvault_db.
 * Records every spend attempt against a VirtualCard, regardless of
 * whether the transaction was approved or declined.
 */
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The virtual card this transaction was attempted against.
     * LAZY-loaded to avoid pulling the full card graph on every query.
     */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "virtual_card_id", nullable = false)
    private VirtualCard virtualCard;

    /**
     * Transaction amount in the base currency.
     * BigDecimal avoids floating-point precision loss for monetary values.
     */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "merchant_name", nullable = false, length = 100)
    private String merchantName;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    /**
     * Outcome of the transaction attempt.
     * SUCCESS — amount was within daily limit; balance updated.
     * DECLINED — amount would exceed daily limit; balance unchanged.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private TransactionStatus status;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    public enum TransactionStatus {
        SUCCESS,
        DECLINED
    }
}

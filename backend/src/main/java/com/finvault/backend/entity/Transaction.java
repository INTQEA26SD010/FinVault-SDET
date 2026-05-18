package com.finvault.backend.entity;

// ─────────────────────────────────────────────────────────────────────────────
// IMPORTS
// ─────────────────────────────────────────────────────────────────────────────
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// ─────────────────────────────────────────────────────────────────────────────
// TRANSACTION ENTITY — Maps to the "transactions" table in MySQL.
// ─────────────────────────────────────────────────────────────────────────────
//
// WHAT IS A TRANSACTION?
// A transaction is a record of someone trying to spend money using a virtual card.
// It stores: which card was used, how much was spent, at which merchant, and
// whether it was approved (SUCCESS) or rejected (DECLINED).
//
// IMPORTANT: We store EVERY attempt — even declined ones!
// Why? For audit/history purposes. The user can see all their spending attempts.
//
// REAL-WORLD ANALOGY:
// Think of your bank statement. It shows every time you swiped your card,
// including failed attempts. This entity is exactly that — a bank statement row.
//
// HOW IT RELATES TO OTHER ENTITIES:
// - Each Transaction belongs to ONE VirtualCard (Many-to-One relationship)
// - A VirtualCard can have MANY Transactions
//
// ─────────────────────────────────────────────────────────────────────────────

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    // ─── PRIMARY KEY ─────────────────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment: 1, 2, 3...
    private Long id;

    // ─── RELATIONSHIP: Many Transactions → One Virtual Card ──────────────────
    //
    // Each transaction is linked to the card that was used.
    // "virtual_card_id" column in the transactions table stores the card's ID.
    //
    // FetchType.LAZY = Don't load the full VirtualCard object unless needed.
    //   This saves memory when we're just listing transactions.
    //
    // optional = false = A transaction MUST be linked to a card (can't exist alone).
    @ToString.Exclude  // Prevent infinite toString() loop
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "virtual_card_id", nullable = false)
    private VirtualCard virtualCard;

    // ─── AMOUNT ──────────────────────────────────────────────────────────────
    // How much money was (or attempted to be) spent in this transaction.
    // Uses BigDecimal for exact money calculations (no floating-point errors).
    //
    // precision = 10, scale = 2 means up to 99,999,999.99
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // ─── MERCHANT NAME ───────────────────────────────────────────────────────
    // The name of the store/service where the money was spent.
    // Examples: "Amazon", "Netflix", "Starbucks", "Uber"
    @Column(name = "merchant_name", nullable = false, length = 100)
    private String merchantName;

    // ─── TIMESTAMP ───────────────────────────────────────────────────────────
    // The exact date and time when this transaction was attempted.
    // "updatable = false" — once set, this can never be changed (immutable).
    // Set automatically by the @PrePersist method below.
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    // ─── STATUS (SUCCESS or DECLINED) ────────────────────────────────────────
    // The outcome of this transaction attempt.
    //
    // @Enumerated(EnumType.STRING) stores "SUCCESS" or "DECLINED" as text in DB
    // (not as numbers 0 or 1 — much more readable when querying the database directly).
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private TransactionStatus status;

    // ─── LIFECYCLE CALLBACK ──────────────────────────────────────────────────
    // @PrePersist runs JUST BEFORE the entity is saved to the database.
    // It sets the timestamp to "right now" automatically — no manual work needed.
    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    // ─── ENUM: Transaction Statuses ──────────────────────────────────────────
    //
    // SUCCESS  → The transaction went through. Money was deducted from the card.
    //            (balance + amount ≤ dailyLimit)
    //
    // DECLINED → The transaction was rejected. Card balance stays the same.
    //            (balance + amount > dailyLimit — would exceed the daily limit!)
    public enum TransactionStatus {
        SUCCESS,
        DECLINED
    }
}

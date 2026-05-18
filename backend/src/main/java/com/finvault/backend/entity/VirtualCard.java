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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// VIRTUAL CARD ENTITY — Maps to the "virtual_cards" table in MySQL.
// ─────────────────────────────────────────────────────────────────────────────
//
// WHAT IS A VIRTUAL CARD?
// Think of it like a temporary debit card you create online for a specific
// purpose (e.g., "Netflix subscription", "Amazon shopping"). Each card has
// a daily spending limit — if you try to spend more than the limit, the
// transaction gets DECLINED. This protects users from overspending.
//
// REAL-WORLD ANALOGY:
// Imagine you give your child a pocket-money card with a ₹500/day limit.
// They can swipe it anywhere, but once they hit ₹500, the card stops working.
// That's exactly what this VirtualCard does!
//
// HOW IT RELATES TO OTHER ENTITIES:
// - Each VirtualCard belongs to ONE User (Many-to-One relationship)
// - Each VirtualCard can have MANY Transactions (One-to-Many relationship)
//
// ─────────────────────────────────────────────────────────────────────────────

@Entity
@Table(name = "virtual_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VirtualCard {

    // ─── PRIMARY KEY ─────────────────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment: 1, 2, 3...
    private Long id;

    // ─── RELATIONSHIP: Many Cards → One User ─────────────────────────────────
    //
    // This is the "owning side" of the relationship (it has the FK column).
    //
    // FetchType.LAZY = "Don't load the full User object unless I explicitly ask for it"
    //   Why? Loading the User every time we fetch a card wastes memory & DB queries.
    //   With LAZY, it only loads when you call card.getUser().
    //
    // optional = false = This card MUST belong to a user (NOT NULL in DB).
    //
    // @JoinColumn = "Create a column called 'user_id' in the virtual_cards table
    //   that stores the ID of the user who owns this card" (Foreign Key)
    @ToString.Exclude  // Prevents infinite loop: Card→User→Cards→User→...
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ─── CARD NUMBER ─────────────────────────────────────────────────────────
    // A randomly generated 16-digit number (like a real card: 4532 1234 5678 9012)
    // "unique = true" ensures no two cards in the system have the same number.
    @Column(name = "card_number", nullable = false, unique = true, length = 16)
    private String cardNumber;

    // ─── EXPIRY DATE ─────────────────────────────────────────────────────────
    // The date until which this card is valid (e.g., 2029-05-18).
    // We set this to 3 years from creation date in the service layer.
    // LocalDate = just a date (no time), maps to SQL DATE type.
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    // ─── CVV (Card Verification Value) ───────────────────────────────────────
    // The 3-digit security code on the back of a card (e.g., "472").
    // Randomly generated when the card is created.
    @Column(name = "cvv", nullable = false, length = 3)
    private String cvv;

    // ─── DAILY LIMIT ─────────────────────────────────────────────────────────
    // The maximum amount this card is allowed to spend per day.
    //
    // WHY BigDecimal INSTEAD OF double?
    // double has floating-point precision issues with money:
    //   0.1 + 0.2 = 0.30000000000000004 (wrong!)
    // BigDecimal handles money calculations EXACTLY:
    //   0.1 + 0.2 = 0.3 (correct!)
    //
    // precision = 10 → total number of digits (e.g., 12345678.90 = 10 digits total)
    // scale = 2    → digits after decimal point (e.g., .90 = 2 decimal places)
    @Column(name = "daily_limit", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyLimit = BigDecimal.ZERO;

    // ─── BALANCE (Amount Spent Today) ────────────────────────────────────────
    // Running total of how much has been spent on this card today.
    // Every successful transaction ADDS to this balance.
    // When balance + new_transaction > dailyLimit → transaction is DECLINED.
    //
    // Example:
    //   dailyLimit = $500, balance = $450
    //   New transaction of $100 → $450 + $100 = $550 > $500 → DECLINED!
    //   New transaction of $30  → $450 + $30  = $480 ≤ $500 → SUCCESS! balance becomes $480
    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    // ─── STATUS ──────────────────────────────────────────────────────────────
    // The current state of the card. Can be: ACTIVE, FROZEN, EXPIRED, or CANCELLED.
    //
    // @Enumerated(EnumType.STRING) means we store the NAME ("ACTIVE", "FROZEN")
    // in the database, NOT the number (0, 1, 2). This is safer because if we
    // reorder the enum values later, the database won't break.
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CardStatus status = CardStatus.ACTIVE;

    // ─── VENDOR NAME ─────────────────────────────────────────────────────────
    // A label describing what this card is for (e.g., "Amazon", "Netflix", "Groceries").
    // Helps users identify their cards quickly on the dashboard.
    //
    // "columnDefinition" gives the exact SQL for this column, including a DEFAULT
    // value of '' (empty string). This ensures existing rows in the DB don't break
    // when we add this new column (Hibernate's ddl-auto=update adds it).
    @Column(name = "vendor_name", columnDefinition = "VARCHAR(100) NOT NULL DEFAULT ''")
    private String vendorName = "";

    // ─── RELATIONSHIP: One Card → Many Transactions ──────────────────────────
    //
    // One card can have many transactions (purchases).
    // CascadeType.ALL = if we delete this card, delete ALL its transactions too.
    // orphanRemoval = true = if a transaction is removed from this list, delete it from DB.
    //
    // This prevents "orphan rows" — transactions pointing to a card that no longer exists.
    @ToString.Exclude
    @OneToMany(mappedBy = "virtualCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    // ─── CREATED AT ──────────────────────────────────────────────────────────
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ─── LIFECYCLE CALLBACK ──────────────────────────────────────────────────
    // Automatically sets createdAt to the current time when the card is first saved.
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ─── ENUM: Card Statuses ─────────────────────────────────────────────────
    // An enum is a fixed set of constants. A card can ONLY be in one of these states:
    //
    //   ACTIVE    → Card is working normally, transactions are allowed
    //   FROZEN    → Card is temporarily disabled (user clicked "Freeze")
    //   EXPIRED   → Card has passed its expiry date
    //   CANCELLED → Card has been permanently cancelled
    public enum CardStatus {
        ACTIVE,
        FROZEN,
        EXPIRED,
        CANCELLED
    }
}

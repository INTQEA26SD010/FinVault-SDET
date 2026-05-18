package com.finvault.backend.repository;

import com.finvault.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// TRANSACTION REPOSITORY — Database access layer for the "transactions" table.
// ─────────────────────────────────────────────────────────────────────────────
//
// This repository handles all database operations for transactions.
// It extends JpaRepository, giving us free CRUD methods:
//   - save(transaction) → INSERT INTO transactions ...
//   - findById(id) → SELECT * FROM transactions WHERE id = ?
//   - findAll() → SELECT * FROM transactions
//   - deleteById(id) → DELETE FROM transactions WHERE id = ?
//
// Below is our custom query method.
//
// ─────────────────────────────────────────────────────────────────────────────

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // ─── FIND TRANSACTIONS BY CARD (NEWEST FIRST) ────────────────────────────
    //
    // "Show me all transactions for card #3, with the newest ones on top"
    //
    // Method name breakdown (Spring reads this like English):
    //   findBy               → SELECT * FROM transactions WHERE ...
    //   VirtualCard          → the relationship field in Transaction entity
    //   Id                   → the .id property of that VirtualCard
    //   OrderBy              → ORDER BY ...
    //   Timestamp            → the timestamp field
    //   Desc                 → DESC (descending = newest first)
    //
    // Generated SQL:
    //   SELECT * FROM transactions
    //   WHERE virtual_card_id = ?
    //   ORDER BY timestamp DESC
    //
    // This is used on the Transactions tab to display the user's spending history.
    List<Transaction> findByVirtualCardIdOrderByTimestampDesc(Long virtualCardId);
}

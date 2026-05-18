package com.finvault.backend.repository;

import com.finvault.backend.entity.VirtualCard;
import com.finvault.backend.entity.VirtualCard.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// ─────────────────────────────────────────────────────────────────────────────
// VIRTUAL CARD REPOSITORY — Database access layer for the "virtual_cards" table.
// ─────────────────────────────────────────────────────────────────────────────
//
// This repository handles all database operations for virtual cards.
// It extends JpaRepository, so we automatically get:
//   - save(), findById(), findAll(), deleteById(), existsById(), count()
//
// Below are CUSTOM query methods. Spring reads the method name and generates SQL:
//
//   Method Name Format:  findBy + FieldName + (And/Or) + FieldName + ...
//   Example:  findByUserId → SELECT * FROM virtual_cards WHERE user_id = ?
//
// ─────────────────────────────────────────────────────────────────────────────

@Repository
public interface VirtualCardRepository extends JpaRepository<VirtualCard, Long> {

    // ─── FIND ALL CARDS BY USER ──────────────────────────────────────────────
    // "Show me all virtual cards belonging to user #5"
    // Used when the dashboard loads — we need to display all the user's cards.
    //
    // Generated SQL: SELECT * FROM virtual_cards WHERE user_id = ?
    // Returns: List of cards (empty list if the user has no cards)
    List<VirtualCard> findByUserId(Long userId);

    // ─── FIND CARD BY CARD NUMBER ────────────────────────────────────────────
    // "Find the card with number 4532789012345678"
    // Could be used during payment processing to look up a card by its number.
    //
    // Generated SQL: SELECT * FROM virtual_cards WHERE card_number = ?
    Optional<VirtualCard> findByCardNumber(String cardNumber);

    // ─── FIND CARDS BY USER AND STATUS ───────────────────────────────────────
    // "Show me only the ACTIVE cards for user #5" (filter out frozen/expired ones)
    // Useful for the Simulator dropdown — you can only transact on ACTIVE cards.
    //
    // Generated SQL: SELECT * FROM virtual_cards WHERE user_id = ? AND status = ?
    //
    // NOTE: CardStatus is an enum, but Spring converts it to a String
    // because we used @Enumerated(EnumType.STRING) on the entity.
    List<VirtualCard> findByUserIdAndStatus(Long userId, CardStatus status);
}

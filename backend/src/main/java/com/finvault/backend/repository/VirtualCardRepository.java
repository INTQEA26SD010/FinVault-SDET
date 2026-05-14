package com.finvault.backend.repository;

import com.finvault.backend.entity.VirtualCard;
import com.finvault.backend.entity.VirtualCard.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the VirtualCard entity.
 * Provides full CRUD operations inherited from JpaRepository<VirtualCard, Long>.
 * All custom finders use Spring Data's derived query method convention.
 */
@Repository
public interface VirtualCardRepository extends JpaRepository<VirtualCard, Long> {

    /**
     * Retrieves all cards belonging to a specific user.
     * Used to render the user's card dashboard.
     * Generates: SELECT * FROM virtual_cards WHERE user_id = ?
     */
    List<VirtualCard> findByUserId(Long userId);

    /**
     * Finds a card by its 16-digit card number.
     * Used during transaction validation.
     * Generates: SELECT * FROM virtual_cards WHERE card_number = ?
     */
    Optional<VirtualCard> findByCardNumber(String cardNumber);

    /**
     * Finds all cards for a user filtered by status (e.g., only ACTIVE cards).
     * Useful for showing "active cards only" in the UI.
     * Generates: SELECT * FROM virtual_cards WHERE user_id = ? AND status = ?
     */
    List<VirtualCard> findByUserIdAndStatus(Long userId, CardStatus status);
}

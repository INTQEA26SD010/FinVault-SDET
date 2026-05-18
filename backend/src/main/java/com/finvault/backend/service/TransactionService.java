package com.finvault.backend.service;

import com.finvault.backend.dto.TransactionRequestDto;
import com.finvault.backend.dto.TransactionResponseDto;
import com.finvault.backend.entity.Transaction;
import com.finvault.backend.entity.Transaction.TransactionStatus;
import com.finvault.backend.entity.VirtualCard;
import com.finvault.backend.repository.TransactionRepository;
import com.finvault.backend.repository.VirtualCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// TRANSACTION SERVICE — The core spending engine for FinVault.
// ─────────────────────────────────────────────────────────────────────────────
//
// WHAT DOES THIS SERVICE DO?
// This is the HEART of FinVault. It decides whether a transaction (purchase)
// should be APPROVED or DECLINED based on the card's daily spending limit.
//
// THE APPROVAL LOGIC (simple rule):
//
//   IF (currentBalance + transactionAmount) ≤ dailyLimit
//       → APPROVE the transaction (add amount to balance)
//
//   IF (currentBalance + transactionAmount) > dailyLimit
//       → DECLINE the transaction (balance stays the same)
//
// EXAMPLE:
//   Card: dailyLimit = $500, balance = $450 (already spent today)
//   New transaction: $100 at Amazon
//   Check: $450 + $100 = $550 > $500 → DECLINED! (would exceed limit)
//
//   New transaction: $30 at Starbucks
//   Check: $450 + $30 = $480 ≤ $500 → SUCCESS! (balance becomes $480)
//
// IMPORTANT: Both SUCCESS and DECLINED transactions are SAVED to the database.
// This gives users a complete history of ALL their spending attempts.
//
// ─────────────────────────────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
@Slf4j  // Lombok: gives us a 'log' object for printing messages to the server console
        // Usage: log.info("message"), log.warn("message"), log.error("message")
public class TransactionService {

    // ─── DEPENDENCIES ────────────────────────────────────────────────────────
    private final TransactionRepository transactionRepository;   // Talks to transactions table
    private final VirtualCardRepository virtualCardRepository;   // Talks to virtual_cards table

    // ─────────────────────────────────────────────────────────────────────────
    // PROCESS TRANSACTION — Main method that approves or declines a purchase
    // ─────────────────────────────────────────────────────────────────────────
    //
    // @Transactional means: if anything fails inside this method, ALL database
    // changes are ROLLED BACK. This prevents inconsistent data.
    // Example: If we update the card balance but fail to save the transaction,
    // the balance change is undone. Database stays consistent!
    //
    // CALLED BY: TransactionController.processTransaction()
    //            when POST /api/transactions is hit
    //
    @Transactional
    public TransactionResponseDto processTransaction(TransactionRequestDto request) {

        // STEP 1: Find the card that's being charged
        // If the card doesn't exist, throw an error immediately
        VirtualCard card = virtualCardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RuntimeException(
                        "VirtualCard not found with id: " + request.getCardId()));

        // STEP 2: Calculate what the balance WOULD BE if we approve this transaction
        // projectedBalance = currentBalance + transactionAmount
        // Example: $450 (current) + $100 (new purchase) = $550 (projected)
        BigDecimal projectedBalance = card.getBalance().add(request.getAmount());

        // STEP 3: Create a new Transaction entity (will be saved regardless of outcome)
        Transaction transaction = new Transaction();
        transaction.setVirtualCard(card);                  // Link to the card
        transaction.setAmount(request.getAmount());        // How much was attempted
        transaction.setMerchantName(request.getMerchantName());  // Where (e.g., "Netflix")

        // STEP 4: THE DECISION — Approve or Decline?
        // compareTo returns: negative if less, 0 if equal, positive if greater
        // So: projectedBalance.compareTo(dailyLimit) <= 0 means "within limit"
        if (projectedBalance.compareTo(card.getDailyLimit()) <= 0) {

            // ─── APPROVED ────────────────────────────────────────────────────
            // The purchase is within the daily limit — go ahead!
            transaction.setStatus(TransactionStatus.SUCCESS);

            // Update the card's balance (add the spent amount)
            card.setBalance(projectedBalance);
            virtualCardRepository.save(card);  // Persist the updated balance

            // Log to server console (helpful for debugging)
            log.info("Transaction APPROVED for card {} | amount={} | newBalance={}",
                    card.getId(), request.getAmount(), projectedBalance);

        } else {

            // ─── DECLINED ────────────────────────────────────────────────────
            // This purchase would exceed the daily limit — reject it!
            // The card's balance is NOT changed (no money deducted).
            transaction.setStatus(TransactionStatus.DECLINED);

            log.warn("Transaction DECLINED for card {} | amount={} | currentBalance={} | dailyLimit={}",
                    card.getId(), request.getAmount(), card.getBalance(), card.getDailyLimit());
        }

        // STEP 5: Save the transaction record to the database
        // (Both SUCCESS and DECLINED transactions are saved for audit/history)
        Transaction saved = transactionRepository.save(transaction);

        // STEP 6: Convert to DTO and return to the controller
        return toResponseDto(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET TRANSACTIONS BY CARD ID — Fetches transaction history for a card
    // ─────────────────────────────────────────────────────────────────────────
    //
    // Returns all transactions (both successful and declined) for a card,
    // ordered by newest first (most recent transaction at the top).
    //
    // @Transactional(readOnly = true) tells Spring this method only READS data,
    // never writes. This allows performance optimizations (like skipping dirty checks).
    //
    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getTransactionsByCardId(Long cardId) {
        return transactionRepository
                .findByVirtualCardIdOrderByTimestampDesc(cardId)  // Get from DB (newest first)
                .stream()                                         // Start stream processing
                .map(this::toResponseDto)                         // Convert each entity → DTO
                .toList();                                        // Collect into a List
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER: Convert Transaction Entity → TransactionResponseDto
    // ─────────────────────────────────────────────────────────────────────────
    private TransactionResponseDto toResponseDto(Transaction tx) {
        TransactionResponseDto dto = new TransactionResponseDto();
        dto.setId(tx.getId());                          // Transaction ID
        dto.setCardId(tx.getVirtualCard().getId());     // Which card was used
        dto.setAmount(tx.getAmount());                  // How much
        dto.setMerchantName(tx.getMerchantName());      // Where
        dto.setTimestamp(tx.getTimestamp());             // When
        dto.setStatus(tx.getStatus().name());           // "SUCCESS" or "DECLINED"
        return dto;
    }
}

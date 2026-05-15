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

/**
 * Core transaction-processing engine for FinVault.
 *
 * <h3>Approval Logic</h3>
 * <pre>
 *   if (card.balance + request.amount) &lt;= card.dailyLimit
 *       → SUCCESS  (balance incremented, card saved)
 *   else
 *       → DECLINED (balance unchanged)
 * </pre>
 *
 * Every attempt (approved or declined) is persisted for audit purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final VirtualCardRepository virtualCardRepository;

    /**
     * Processes an incoming transaction request against a virtual card.
     *
     * @param request DTO containing cardId, amount, and merchantName
     * @return TransactionResponseDto with the outcome (SUCCESS / DECLINED)
     * @throws RuntimeException if the card ID does not exist
     */
    @Transactional
    public TransactionResponseDto processTransaction(TransactionRequestDto request) {

        VirtualCard card = virtualCardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RuntimeException(
                        "VirtualCard not found with id: " + request.getCardId()));

        BigDecimal projectedBalance = card.getBalance().add(request.getAmount());
        Transaction transaction = new Transaction();
        transaction.setVirtualCard(card);
        transaction.setAmount(request.getAmount());
        transaction.setMerchantName(request.getMerchantName());

        if (projectedBalance.compareTo(card.getDailyLimit()) <= 0) {
            // — Approved: update card balance —
            transaction.setStatus(TransactionStatus.SUCCESS);
            card.setBalance(projectedBalance);
            virtualCardRepository.save(card);
            log.info("Transaction APPROVED for card {} | amount={} | newBalance={}",
                    card.getId(), request.getAmount(), projectedBalance);
        } else {
            // — Declined: daily limit would be exceeded —
            transaction.setStatus(TransactionStatus.DECLINED);
            log.warn("Transaction DECLINED for card {} | amount={} | currentBalance={} | dailyLimit={}",
                    card.getId(), request.getAmount(), card.getBalance(), card.getDailyLimit());
        }

        Transaction saved = transactionRepository.save(transaction);
        return toResponseDto(saved);
    }

    // ── Private helpers ──────────────────────────────────────────────

    private TransactionResponseDto toResponseDto(Transaction tx) {
        TransactionResponseDto dto = new TransactionResponseDto();
        dto.setId(tx.getId());
        dto.setCardId(tx.getVirtualCard().getId());
        dto.setAmount(tx.getAmount());
        dto.setMerchantName(tx.getMerchantName());
        dto.setTimestamp(tx.getTimestamp());
        dto.setStatus(tx.getStatus().name());
        return dto;
    }
}

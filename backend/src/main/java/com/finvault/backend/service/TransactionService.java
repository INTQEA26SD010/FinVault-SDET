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



@Service
@RequiredArgsConstructor
@Slf4j  // Lombok: gives us a 'log' object for printing messages to the server console
        // Usage: log.info("message"), log.warn("message"), log.error("message")
public class TransactionService {

    
    private final TransactionRepository transactionRepository;   // Talks to transactions table
    private final VirtualCardRepository virtualCardRepository;   // Talks to virtual_cards table

   
    @Transactional
    public TransactionResponseDto processTransaction(TransactionRequestDto request) {

        
        VirtualCard card = virtualCardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RuntimeException(
                        "VirtualCard not found with id: " + request.getCardId()));

        
        BigDecimal projectedBalance = card.getBalance().add(request.getAmount());

       
        Transaction transaction = new Transaction();
        transaction.setVirtualCard(card);                  // Link to the card
        transaction.setAmount(request.getAmount());        // How much was attempted
        transaction.setMerchantName(request.getMerchantName());  // Where (e.g., "Netflix")

        
        if (projectedBalance.compareTo(card.getDailyLimit()) <= 0) {

           
            transaction.setStatus(TransactionStatus.SUCCESS);

            // Update the card's balance (add the spent amount)
            card.setBalance(projectedBalance);
            virtualCardRepository.save(card);  // Persist the updated balance

            // Log to server console (helpful for debugging)
            log.info("Transaction APPROVED for card {} | amount={} | newBalance={}",
                    card.getId(), request.getAmount(), projectedBalance);

        } else {

           
            transaction.setStatus(TransactionStatus.DECLINED);

            log.warn("Transaction DECLINED for card {} | amount={} | currentBalance={} | dailyLimit={}",
                    card.getId(), request.getAmount(), card.getBalance(), card.getDailyLimit());
        }

        
        Transaction saved = transactionRepository.save(transaction);

        return toResponseDto(saved);
    }

    
    
    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getTransactionsByCardId(Long cardId) {
        return transactionRepository
                .findByVirtualCardIdOrderByTimestampDesc(cardId)  // Get from DB (newest first)
                .stream()                                         // Start stream processing
                .map(this::toResponseDto)                         // Convert each entity → DTO
                .toList();                                        // Collect into a List
    }

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

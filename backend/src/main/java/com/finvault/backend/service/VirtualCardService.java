package com.finvault.backend.service;

import com.finvault.backend.dto.CreateVirtualCardDto;
import com.finvault.backend.dto.VirtualCardResponseDto;
import com.finvault.backend.entity.User;
import com.finvault.backend.entity.VirtualCard;
import com.finvault.backend.repository.UserRepository;
import com.finvault.backend.repository.VirtualCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor  // Auto-creates constructor: VirtualCardService(repo, userRepo, random)
public class VirtualCardService {

   
    private final VirtualCardRepository virtualCardRepository;  // Talks to virtual_cards table
    private final UserRepository userRepository;                // Talks to users table
    private final Random random = new Random();                 // For generating random card numbers

    
    public List<VirtualCardResponseDto> getCardsByUserId(Long userId) {
        
        List<VirtualCard> cards = virtualCardRepository.findByUserId(userId);

       
        return cards.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    
    public VirtualCardResponseDto createVirtualCard(CreateVirtualCardDto request) {

        // Find the user — if not found, throw an error
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException(
                        "User not found with ID: " + request.getUserId()));

        // Build the new card entity
        VirtualCard card = new VirtualCard();
        card.setUser(user);                                    // Link card to the user
        card.setCardNumber(generateCardNumber());              // Random 16 digits
        card.setCvv(generateCvv());                            // Random 3 digits
        card.setExpiryDate(LocalDate.now().plusYears(3));       // Valid for 3 years
        card.setDailyLimit(request.getDailyLimit() != null     // Set daily spending limit
                ? request.getDailyLimit()
                : BigDecimal.ZERO);
        card.setVendorName(request.getVendorName() != null     // Set vendor label
                ? request.getVendorName().trim()
                : "");
        card.setStatus(VirtualCard.CardStatus.ACTIVE);         // New cards start as ACTIVE

        // Save to database — MySQL generates the ID automatically
        VirtualCard saved = virtualCardRepository.save(card);

        // Convert to DTO and return to the controller
        return toResponseDto(saved);
    }

    
    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));  // Append a random digit 0-9
        }
        return sb.toString();
    }

   
    private String generateCvv() {
        return String.format("%03d", random.nextInt(1000));  // 0 to 999, padded to 3 digits
    }

    
    @Transactional
    public VirtualCardResponseDto toggleCardStatus(Long id) {
        // Find the card — throw error if it doesn't exist
        VirtualCard card = virtualCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + id));

        // Flip the status: ACTIVE → FROZEN or FROZEN → ACTIVE
        // This is a ternary expression: condition ? valueIfTrue : valueIfFalse
        card.setStatus(card.getStatus() == VirtualCard.CardStatus.ACTIVE
                ? VirtualCard.CardStatus.FROZEN    // Was active → freeze it
                : VirtualCard.CardStatus.ACTIVE);  // Was frozen → unfreeze it

        // Save the updated card and return as DTO
        return toResponseDto(virtualCardRepository.save(card));
    }

  
    @Transactional
    public void deleteCard(Long id) {
        // First check if the card exists — if not, throw an error
        if (!virtualCardRepository.existsById(id)) {
            throw new RuntimeException("Card not found with ID: " + id);
        }
        // Delete the card (and all its transactions due to cascade)
        virtualCardRepository.deleteById(id);
    }

   
    private VirtualCardResponseDto toResponseDto(VirtualCard card) {
        return new VirtualCardResponseDto(
                card.getId(),               // Database ID
                card.getCardNumber(),       // "4532789012345678"
                card.getCvv(),              // "291"
                card.getDailyLimit(),       // 500.00
                card.getBalance(),          // 120.50 (amount spent today)
                card.getStatus().name(),    // "ACTIVE" or "FROZEN" (enum → String)
                card.getVendorName()        // "Amazon" or "Netflix"
        );
    }
}

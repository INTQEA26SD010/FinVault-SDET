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

/**
 * Business logic for virtual card operations.
 * Maps VirtualCard entities to VirtualCardResponseDto objects,
 * ensuring sensitive fields (cvv, raw user FK) are never exposed.
 */
@Service
@RequiredArgsConstructor
public class VirtualCardService {

    private final VirtualCardRepository virtualCardRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    /**
     * Retrieves all virtual cards belonging to the given user.
     * Maps each VirtualCard entity to a safe VirtualCardResponseDto
     * before returning — the CVV is never included in the response.
     *
     * @param userId the ID of the user whose cards should be fetched
     * @return list of card response DTOs (empty list if user has no cards)
     */
    public List<VirtualCardResponseDto> getCardsByUserId(Long userId) {
        List<VirtualCard> cards = virtualCardRepository.findByUserId(userId);

        return cards.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new virtual card for the specified user.
     * Auto-generates a random 16-digit card number, 3-digit CVV,
     * and sets the expiry date to 3 years from today.
     *
     * @param request DTO containing userId and dailyLimit
     * @return the created card mapped to a response DTO
     * @throws RuntimeException if the user is not found
     */
    public VirtualCardResponseDto createVirtualCard(CreateVirtualCardDto request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException(
                        "User not found with ID: " + request.getUserId()));

        VirtualCard card = new VirtualCard();
        card.setUser(user);
        card.setCardNumber(generateCardNumber());
        card.setCvv(generateCvv());
        card.setExpiryDate(LocalDate.now().plusYears(3));
        card.setDailyLimit(request.getDailyLimit() != null ? request.getDailyLimit() : BigDecimal.ZERO);
        card.setVendorName(request.getVendorName() != null ? request.getVendorName().trim() : "");
        card.setStatus(VirtualCard.CardStatus.ACTIVE);

        VirtualCard saved = virtualCardRepository.save(card);

        return toResponseDto(saved);
    }

    /**
     * Generates a random 16-digit card number string.
     */
    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Generates a random 3-digit CVV string (000–999).
     */
    private String generateCvv() {
        return String.format("%03d", random.nextInt(1000));
    }

    /**
     * Toggles a card's status between ACTIVE and FROZEN.
     *
     * @param id the card's primary key
     * @return updated card response DTO
     * @throws RuntimeException if the card is not found
     */
    @Transactional
    public VirtualCardResponseDto toggleCardStatus(Long id) {
        VirtualCard card = virtualCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + id));
        card.setStatus(card.getStatus() == VirtualCard.CardStatus.ACTIVE
                ? VirtualCard.CardStatus.FROZEN
                : VirtualCard.CardStatus.ACTIVE);
        return toResponseDto(virtualCardRepository.save(card));
    }

    /**
     * Permanently deletes a card by its ID.
     *
     * @param id the card's primary key
     * @throws RuntimeException if the card is not found
     */
    @Transactional
    public void deleteCard(Long id) {
        if (!virtualCardRepository.existsById(id)) {
            throw new RuntimeException("Card not found with ID: " + id);
        }
        virtualCardRepository.deleteById(id);
    }

    /**
     * Maps a VirtualCard entity to its safe response DTO.
     * Centralises field mapping so all service methods stay consistent.
     */
    private VirtualCardResponseDto toResponseDto(VirtualCard card) {
        return new VirtualCardResponseDto(
                card.getId(),
                card.getCardNumber(),
                card.getCvv(),
                card.getDailyLimit(),
                card.getBalance(),
                card.getStatus().name(),
                card.getVendorName()
        );
    }
}

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

// ─────────────────────────────────────────────────────────────────────────────
// VIRTUAL CARD SERVICE — Business logic for creating, fetching, toggling, and
//                        deleting virtual cards.
// ─────────────────────────────────────────────────────────────────────────────
//
// WHAT DOES THIS SERVICE DO?
// 1. getCardsByUserId()   → Fetch all cards for a user (display on dashboard)
// 2. createVirtualCard()  → Generate a new card with random number/CVV/expiry
// 3. toggleCardStatus()   → Freeze or unfreeze a card (ACTIVE ↔ FROZEN)
// 4. deleteCard()         → Permanently remove a card from the system
//
// The Service layer ensures:
// - Sensitive data (like the full User entity) is never exposed to the API
// - Business rules are enforced (e.g., card must exist before toggling)
// - Entity ↔ DTO conversion is centralized in one place
//
// ─────────────────────────────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor  // Auto-creates constructor: VirtualCardService(repo, userRepo, random)
public class VirtualCardService {

    // ─── DEPENDENCIES ────────────────────────────────────────────────────────
    private final VirtualCardRepository virtualCardRepository;  // Talks to virtual_cards table
    private final UserRepository userRepository;                // Talks to users table
    private final Random random = new Random();                 // For generating random card numbers

    // ─────────────────────────────────────────────────────────────────────────
    // GET CARDS BY USER ID — Returns all virtual cards owned by a user
    // ─────────────────────────────────────────────────────────────────────────
    //
    // FLOW:
    //   1. Ask the repository: "Give me all cards where user_id = X"
    //   2. Convert each VirtualCard entity to a VirtualCardResponseDto
    //   3. Return the list of DTOs
    //
    // WHY CONVERT TO DTO?
    // The VirtualCard entity has a reference to the User object (which contains
    // passwordHash!). We must NEVER send that to the frontend. The DTO only
    // contains safe fields: id, cardNumber, cvv, dailyLimit, balance, status, vendorName.
    //
    public List<VirtualCardResponseDto> getCardsByUserId(Long userId) {
        // Fetch all cards from database where user_id matches
        List<VirtualCard> cards = virtualCardRepository.findByUserId(userId);

        // Convert each entity to a DTO using Java Streams
        // .stream() = start processing the list one-by-one
        // .map(this::toResponseDto) = for each card, call toResponseDto() to convert it
        // .collect(Collectors.toList()) = gather all converted DTOs back into a List
        return cards.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE VIRTUAL CARD — Generates a new card for the user
    // ─────────────────────────────────────────────────────────────────────────
    //
    // FLOW:
    //   1. Find the user who's creating the card (fail if user doesn't exist)
    //   2. Create a new VirtualCard entity with:
    //      - Random 16-digit card number (like 4532789012345678)
    //      - Random 3-digit CVV (like 291)
    //      - Expiry date = today + 3 years
    //      - Daily limit from the request
    //      - Vendor name from the request
    //      - Status = ACTIVE (card is ready to use immediately)
    //   3. Save to database
    //   4. Return the created card as a DTO
    //
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

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER: Generate a random 16-digit card number
    // ─────────────────────────────────────────────────────────────────────────
    // Example output: "4532789012345678"
    // Uses StringBuilder for efficiency (better than string concatenation in a loop)
    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));  // Append a random digit 0-9
        }
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER: Generate a random 3-digit CVV
    // ─────────────────────────────────────────────────────────────────────────
    // Example output: "042", "891", "007"
    // %03d ensures zero-padding: 7 becomes "007", 42 becomes "042"
    private String generateCvv() {
        return String.format("%03d", random.nextInt(1000));  // 0 to 999, padded to 3 digits
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOGGLE CARD STATUS — Freeze or Unfreeze a card
    // ─────────────────────────────────────────────────────────────────────────
    //
    // LOGIC:
    //   - If card is ACTIVE  → change to FROZEN  (card can't be used)
    //   - If card is FROZEN  → change to ACTIVE  (card works again)
    //
    // @Transactional ensures that if something fails mid-way, ALL changes
    // are rolled back (the database stays consistent). It's like an "undo"
    // button for database operations.
    //
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

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE CARD — Permanently removes a card from the database
    // ─────────────────────────────────────────────────────────────────────────
    //
    // Because the VirtualCard entity has cascade = ALL on its transactions,
    // deleting a card also AUTOMATICALLY deletes all its transactions.
    // (Otherwise MySQL would throw a foreign key constraint error)
    //
    @Transactional
    public void deleteCard(Long id) {
        // First check if the card exists — if not, throw an error
        if (!virtualCardRepository.existsById(id)) {
            throw new RuntimeException("Card not found with ID: " + id);
        }
        // Delete the card (and all its transactions due to cascade)
        virtualCardRepository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER: Convert VirtualCard Entity → VirtualCardResponseDto
    // ─────────────────────────────────────────────────────────────────────────
    //
    // This method is used by ALL the above methods to ensure consistent
    // entity-to-DTO mapping. All fields are carefully selected — we include
    // only what the frontend needs.
    //
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

package com.finvault.backend.controller;

import com.finvault.backend.dto.CreateVirtualCardDto;
import com.finvault.backend.dto.VirtualCardResponseDto;
import com.finvault.backend.service.VirtualCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for virtual card management operations.
 * Base path: /api/cards
 */
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class VirtualCardController {

    private final VirtualCardService virtualCardService;

    /**
     * GET /api/cards/user/{userId}
     *
     * Returns all virtual cards owned by the specified user.
     * CVV and raw user FK are excluded from the response DTO.
     * Returns 200 OK with an empty list [] if the user has no cards.
     *
     * Example response:
     * [
     *   {
     *     "id": 1,
     *     "cardNumber": "4111111111111111",
     *     "dailyLimit": 500.00,
     *     "status": "ACTIVE"
     *   }
     * ]
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VirtualCardResponseDto>> getCardsByUser(
            @PathVariable Long userId) {
        List<VirtualCardResponseDto> cards = virtualCardService.getCardsByUserId(userId);
        return ResponseEntity.ok(cards);
    }

    /**
     * POST /api/cards
     *
     * Creates a new virtual card for the specified user.
     * Accepts a JSON body with userId and dailyLimit.
     * Auto-generates cardNumber (16 digits), cvv (3 digits),
     * expiryDate (3 years from today), balance (0.0), status (ACTIVE).
     *
     * Example request body:
     * {
     *   "userId": 1,
     *   "dailyLimit": 500.00
     * }
     *
     * Returns 201 CREATED with the new card's response DTO.
     */
    @PostMapping
    public ResponseEntity<VirtualCardResponseDto> createCard(
            @RequestBody CreateVirtualCardDto request) {
        VirtualCardResponseDto created = virtualCardService.createVirtualCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}

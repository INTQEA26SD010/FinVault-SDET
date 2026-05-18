package com.finvault.backend.controller;

import com.finvault.backend.dto.CreateVirtualCardDto;
import com.finvault.backend.dto.VirtualCardResponseDto;
import com.finvault.backend.service.VirtualCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// VIRTUAL CARD CONTROLLER — REST API for card management (CRUD operations).
// ─────────────────────────────────────────────────────────────────────────────
//
// This controller provides endpoints for:
//   1. GET    /api/cards/user/{userId}    → List all cards for a user
//   2. POST   /api/cards                  → Create a new virtual card
//   3. PUT    /api/cards/{id}/toggle      → Freeze or unfreeze a card
//   4. DELETE /api/cards/{id}             → Permanently delete a card
//
// HTTP METHODS EXPLAINED:
//   GET    = Read data (safe, no side effects)
//   POST   = Create new data
//   PUT    = Update existing data
//   DELETE = Remove data
//
// RESPONSE STATUS CODES:
//   200 OK         = Request successful, here's the data
//   201 CREATED    = New resource was created successfully
//   204 NO CONTENT = Successful but nothing to return (used for DELETE)
//   404 NOT FOUND  = The requested resource doesn't exist
//
// ─────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/cards")     // Base URL: all endpoints start with /api/cards
@RequiredArgsConstructor
public class VirtualCardController {

    // The service that contains the actual business logic for card operations
    private final VirtualCardService virtualCardService;

    // ─────────────────────────────────────────────────────────────────────────
    // GET ALL CARDS FOR A USER — GET /api/cards/user/{userId}
    // ─────────────────────────────────────────────────────────────────────────
    //
    // EXAMPLE: GET /api/cards/user/5
    //   → Returns all virtual cards owned by user #5
    //
    // @PathVariable means: extract the {userId} from the URL path
    //   e.g., /api/cards/user/5 → userId = 5
    //
    // Returns: 200 OK + JSON array of cards (or empty array [] if no cards)
    //
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VirtualCardResponseDto>> getCardsByUser(
            @PathVariable Long userId) {
        List<VirtualCardResponseDto> cards = virtualCardService.getCardsByUserId(userId);
        return ResponseEntity.ok(cards);  // 200 OK + cards as JSON
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE A NEW CARD — POST /api/cards
    // ─────────────────────────────────────────────────────────────────────────
    //
    // WHAT THE FRONTEND SENDS (JSON body):
    // { "userId": 5, "dailyLimit": 500.00, "vendorName": "Amazon" }
    //
    // WHAT HAPPENS:
    // 1. Backend generates random cardNumber, CVV, expiryDate
    // 2. Creates the card with status=ACTIVE, balance=0
    // 3. Saves to database
    // 4. Returns the new card as a DTO
    //
    // Returns: 201 CREATED + the new card's JSON
    //
    @PostMapping
    public ResponseEntity<VirtualCardResponseDto> createCard(
            @RequestBody CreateVirtualCardDto request) {
        VirtualCardResponseDto created = virtualCardService.createVirtualCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);  // 201 + card JSON
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOGGLE CARD STATUS — PUT /api/cards/{id}/toggle
    // ─────────────────────────────────────────────────────────────────────────
    //
    // WHAT IT DOES:
    //   ACTIVE → FROZEN  (card is frozen, transactions will be declined)
    //   FROZEN → ACTIVE  (card is unfrozen, works normally again)
    //
    // EXAMPLE: PUT /api/cards/3/toggle
    //   → Flips card #3 between ACTIVE and FROZEN
    //
    // Returns:
    //   200 OK + updated card JSON (if card exists)
    //   404 NOT FOUND (if card doesn't exist)
    //
    @PutMapping("/{id}/toggle")
    public ResponseEntity<VirtualCardResponseDto> toggleStatus(@PathVariable Long id) {
        try {
            VirtualCardResponseDto updated = virtualCardService.toggleCardStatus(id);
            return ResponseEntity.ok(updated);  // 200 OK + updated card
        } catch (RuntimeException e) {
            // Card not found → return 404 with empty body
            return ResponseEntity.notFound().build();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE A CARD — DELETE /api/cards/{id}
    // ─────────────────────────────────────────────────────────────────────────
    //
    // WHAT IT DOES:
    // Permanently removes a virtual card AND all its transactions from the database.
    // (Transactions are deleted automatically due to CascadeType.ALL on the entity)
    //
    // EXAMPLE: DELETE /api/cards/3
    //   → Deletes card #3 and all its transaction history
    //
    // Returns:
    //   204 NO CONTENT (successful deletion — nothing to return)
    //   404 NOT FOUND (if card doesn't exist)
    //
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        try {
            virtualCardService.deleteCard(id);
            return ResponseEntity.noContent().build();  // 204 = deleted successfully
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();   // 404 = card doesn't exist
        }
    }
}

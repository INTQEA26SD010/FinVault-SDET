package com.finvault.backend.controller;

import com.finvault.backend.dto.CreateVirtualCardDto;
import com.finvault.backend.dto.VirtualCardResponseDto;
import com.finvault.backend.service.VirtualCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/api/cards")     // Base URL: all endpoints start with /api/cards
@RequiredArgsConstructor
public class VirtualCardController {

    // The service that contains the actual business logic for card operations
    private final VirtualCardService virtualCardService;

    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VirtualCardResponseDto>> getCardsByUser(
            @PathVariable Long userId) {
        List<VirtualCardResponseDto> cards = virtualCardService.getCardsByUserId(userId);
        return ResponseEntity.ok(cards);  // 200 OK + cards as JSON
    }

    
    @PostMapping
    public ResponseEntity<VirtualCardResponseDto> createCard(
            @RequestBody CreateVirtualCardDto request) {
        VirtualCardResponseDto created = virtualCardService.createVirtualCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);  // 201 + card JSON
    }

    
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

package com.finvault.backend.controller;

import com.finvault.backend.dto.TransactionRequestDto;
import com.finvault.backend.dto.TransactionResponseDto;
import com.finvault.backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for transaction simulation.
 * Base path: /api/transactions
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * GET /api/transactions/card/{cardId}
     *
     * Returns all transactions for the given virtual card, ordered newest-first.
     * Returns an empty list [] when no transactions exist yet.
     *
     * @param cardId the virtual card's primary key
     */
    @GetMapping("/card/{cardId}")
    public ResponseEntity<List<TransactionResponseDto>> getByCard(
            @PathVariable Long cardId) {
        return ResponseEntity.ok(transactionService.getTransactionsByCardId(cardId));
    }

    /**
     * POST /api/transactions
     *
     * Simulates a spend transaction against a virtual card.
     * The response contains the transaction record with a status
     * of either SUCCESS or DECLINED based on the daily-limit check.
     *
     * @param request JSON body with cardId, amount, merchantName
     * @return the saved Transaction wrapped in a response DTO
     */
    @PostMapping
    public ResponseEntity<TransactionResponseDto> processTransaction(
            @RequestBody TransactionRequestDto request) {
        TransactionResponseDto response = transactionService.processTransaction(request);
        HttpStatus status = "SUCCESS".equals(response.getStatus())
                ? HttpStatus.OK
                : HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(status).body(response);
    }
}

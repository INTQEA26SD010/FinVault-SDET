package com.finvault.backend.controller;

import com.finvault.backend.dto.TransactionRequestDto;
import com.finvault.backend.dto.TransactionResponseDto;
import com.finvault.backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// TRANSACTION CONTROLLER — REST API for transaction simulation and history.
// ─────────────────────────────────────────────────────────────────────────────
//
// This controller provides endpoints for:
//   1. GET  /api/transactions/card/{cardId}  → Get all transactions for a card
//   2. POST /api/transactions                → Simulate a new transaction (purchase)
//
// HOW THE SIMULATOR WORKS:
// The Angular frontend has a "Simulator" page where users can test purchases.
// They select a card, enter an amount and merchant name, then click "Simulate".
// This sends a POST request here, and the backend decides: approve or decline?
//
// ─────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/transactions")  // Base URL: /api/transactions
@RequiredArgsConstructor
public class TransactionController {

    // The service that contains the transaction processing logic
    private final TransactionService transactionService;

    // ─────────────────────────────────────────────────────────────────────────
    // GET TRANSACTIONS BY CARD — GET /api/transactions/card/{cardId}
    // ─────────────────────────────────────────────────────────────────────────
    //
    // WHAT IT DOES:
    // Returns ALL transactions (both successful and declined) for a specific card,
    // ordered by newest first.
    //
    // EXAMPLE: GET /api/transactions/card/3
    //   → Returns all transactions for card #3
    //
    // The frontend calls this for EACH card the user owns, then combines them
    // all together to show the full transaction history on the Transactions tab.
    //
    // Returns: 200 OK + JSON array of transactions (or empty [] if none)
    //
    @GetMapping("/card/{cardId}")
    public ResponseEntity<List<TransactionResponseDto>> getByCard(
            @PathVariable Long cardId) {
        return ResponseEntity.ok(transactionService.getTransactionsByCardId(cardId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PROCESS TRANSACTION — POST /api/transactions
    // ─────────────────────────────────────────────────────────────────────────
    //
    // WHAT IT DOES:
    // Simulates a purchase (transaction) against a virtual card.
    // The backend checks whether the card has enough remaining daily limit.
    //
    // WHAT THE FRONTEND SENDS (JSON body):
    // { "cardId": 3, "amount": 49.99, "merchantName": "Netflix" }
    //
    // DECISION LOGIC (in TransactionService):
    //   IF (currentBalance + amount) ≤ dailyLimit → SUCCESS (money charged)
    //   IF (currentBalance + amount) > dailyLimit → DECLINED (over limit)
    //
    // WHAT WE RETURN:
    //   If SUCCESS  → 200 OK + transaction details
    //   If DECLINED → 422 UNPROCESSABLE ENTITY + transaction details
    //
    // WHY 422 FOR DECLINED?
    // HTTP 422 means "I understood your request, but I can't process it due to
    // business rules." This is different from 400 (bad syntax) — the request
    // format was correct, but the business rule (daily limit) prevented it.
    //
    @PostMapping
    public ResponseEntity<TransactionResponseDto> processTransaction(
            @RequestBody TransactionRequestDto request) {

        // Call the service to process the transaction
        TransactionResponseDto response = transactionService.processTransaction(request);

        // Choose the HTTP status based on the transaction outcome
        HttpStatus status = "SUCCESS".equals(response.getStatus())
                ? HttpStatus.OK                       // 200 for approved transactions
                : HttpStatus.UNPROCESSABLE_ENTITY;    // 422 for declined transactions

        return ResponseEntity.status(status).body(response);
    }
}

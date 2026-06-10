package com.finvault.backend.controller;

import com.finvault.backend.dto.TransactionRequestDto;
import com.finvault.backend.dto.TransactionResponseDto;
import com.finvault.backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/api/transactions")  // Base URL: /api/transactions
@RequiredArgsConstructor
public class TransactionController {

    // The service that contains the transaction processing logic
    private final TransactionService transactionService;

  
    @GetMapping("/card/{cardId}")
    public ResponseEntity<List<TransactionResponseDto>> getByCard(
            @PathVariable Long cardId) {
        return ResponseEntity.ok(transactionService.getTransactionsByCardId(cardId));
    }

    
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

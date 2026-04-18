package com.jp.transaction.controller;

import com.jp.transaction.dto.TransactionDTO;
import com.jp.transaction.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(
            @RequestBody TransactionDTO transactionDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(transactionDTO));
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @GetMapping("/sender/{senderId}")
    public ResponseEntity<List<TransactionDTO>> getBySenderId(
            @PathVariable Long senderId) {
        return ResponseEntity.ok(transactionService.getTransactionsBySenderId(senderId));
    }

    @GetMapping("/receiver/{receiverId}")
    public ResponseEntity<List<TransactionDTO>> getByReceiverId(
            @PathVariable Long receiverId) {
        return ResponseEntity.ok(transactionService.getTransactionsByReceiverId(receiverId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TransactionDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(transactionService.updateTransactionStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}

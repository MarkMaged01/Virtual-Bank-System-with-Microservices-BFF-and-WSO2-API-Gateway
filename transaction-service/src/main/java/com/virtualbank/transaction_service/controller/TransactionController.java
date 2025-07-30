package com.virtualbank.transaction_service.controller;

import com.virtualbank.transaction_service.model.Transaction;
import com.virtualbank.transaction_service.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping("/transfer/initiation")
    public ResponseEntity<?> initiateTransfer(@RequestBody Map<String, Object> request) {
        try {
            UUID fromAccountId = UUID.fromString(request.get("fromAccountId").toString());
            UUID toAccountId = UUID.fromString(request.get("toAccountId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String description = request.getOrDefault("description", "").toString();
            Optional<Transaction> txOpt = service.initiateTransfer(fromAccountId, toAccountId, amount, description);
            if (txOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", 400,
                    "error", "Bad Request",
                    "message", "Invalid input."
                ));
            }
            Transaction tx = txOpt.get();
            return ResponseEntity.ok(Map.of(
                "transactionId", tx.getTransactionId(),
                "status", tx.getStatus(),
                "timestamp", tx.getTimestamp()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/transfer/execution")
    public ResponseEntity<?> executeTransfer(@RequestBody Map<String, Object> request) {
        try {
            UUID transactionId = UUID.fromString(request.get("transactionId").toString());
            Map<String, Object> result = service.executeTransfer(transactionId);
            if (result.containsKey("error")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", 400,
                    "error", "Bad Request",
                    "message", result.get("error")
                ));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<?> getAccountTransactions(@PathVariable String accountId) {
        try {
            UUID accId = UUID.fromString(accountId);
            List<Transaction> txs = service.getTransactionsForAccount(accId);
            if (txs.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", 404,
                    "error", "Not Found",
                    "message", "No transactions found for account ID " + accountId + "."
                ));
            }
            List<Map<String, Object>> result = new ArrayList<>();
            for (Transaction tx : txs) {
                result.add(Map.of(
                    "transactionId", tx.getTransactionId(),
                    "accountId", accId,
                    "amount", tx.getFromAccountId() != null && tx.getFromAccountId().equals(accId) ? tx.getAmount().negate() : tx.getAmount(),
                    "description", tx.getDescription(),
                    "timestamp", tx.getTimestamp()
                ));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/test")
    public String test() { return "ok"; }
} 
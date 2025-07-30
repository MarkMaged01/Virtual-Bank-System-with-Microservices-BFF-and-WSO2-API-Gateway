package com.virtualbank.transaction_service.service;

import com.virtualbank.transaction_service.dto.TransactionRequestDto;
import com.virtualbank.transaction_service.exception.TransactionNotFoundException;
import com.virtualbank.transaction_service.model.*;
import com.virtualbank.transaction_service.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private RestTemplate restTemplate;

    private static final String ACCOUNT_SERVICE_URL = "http://localhost:8081";

    public Optional<Transaction> initiateTransfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String description) {
        try {
            // Create transaction with Initiated status
            Transaction transaction = new Transaction();
            transaction.setFromAccountId(fromAccountId);
            transaction.setToAccountId(toAccountId);
            transaction.setAmount(amount);
            transaction.setDescription(description != null ? description : "");
            transaction.setStatus(TransactionStatus.Initiated);
            transaction.setTimestamp(LocalDateTime.now());

            Transaction savedTransaction = transactionRepository.save(transaction);
            return Optional.of(savedTransaction);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Transactional
    public Map<String, Object> executeTransfer(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

        if (transaction.getStatus() != TransactionStatus.Initiated) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Transaction is not in Initiated status");
            return error;
        }

        try {
            // Call Account Service to perform the transfer
            Map<String, Object> transferRequest = new HashMap<>();
            transferRequest.put("fromAccountId", transaction.getFromAccountId());
            transferRequest.put("toAccountId", transaction.getToAccountId());
            transferRequest.put("amount", transaction.getAmount());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(transferRequest, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                ACCOUNT_SERVICE_URL + "/accounts/transfer",
                HttpMethod.PUT,
                request,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                // Update transaction status to Success
                transaction.setStatus(TransactionStatus.Success);
                transactionRepository.save(transaction);

                Map<String, Object> result = new HashMap<>();
                result.put("transactionId", transaction.getTransactionId());
                result.put("status", transaction.getStatus());
                result.put("timestamp", transaction.getTimestamp());
                return result;
            } else {
                // Update transaction status to Failed
                transaction.setStatus(TransactionStatus.Failed);
                transactionRepository.save(transaction);

                Map<String, Object> error = new HashMap<>();
                error.put("error", "Transfer failed");
                return error;
            }
        } catch (Exception e) {
            // Update transaction status to Failed
            transaction.setStatus(TransactionStatus.Failed);
            transactionRepository.save(transaction);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Transfer failed: " + e.getMessage());
            return error;
        }
    }

    public List<Transaction> getTransactionsForAccount(UUID accountId) {
        return transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId);
    }
} 
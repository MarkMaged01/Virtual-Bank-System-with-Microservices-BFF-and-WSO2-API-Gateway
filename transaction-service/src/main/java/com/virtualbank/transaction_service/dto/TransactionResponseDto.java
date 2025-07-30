package com.virtualbank.transaction_service.dto;

import com.virtualbank.transaction_service.model.TransactionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionResponseDto {
    private UUID transactionId;
    private TransactionStatus status;
    private LocalDateTime timestamp;

    // Constructors
    public TransactionResponseDto() {}
    
    public TransactionResponseDto(UUID transactionId, TransactionStatus status, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
} 
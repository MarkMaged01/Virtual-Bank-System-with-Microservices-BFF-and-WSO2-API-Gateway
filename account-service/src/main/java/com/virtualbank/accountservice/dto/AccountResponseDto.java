package com.virtualbank.accountservice.dto;

import com.virtualbank.accountservice.model.AccountStatus;
import com.virtualbank.accountservice.model.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountResponseDto {
    private UUID accountId;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private AccountStatus status;
    private String message;

    // Constructors
    public AccountResponseDto() {}
    
    public AccountResponseDto(UUID accountId, String accountNumber, String message) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.message = message;
    }
    
    public AccountResponseDto(UUID accountId, String accountNumber, AccountType accountType, 
                            BigDecimal balance, AccountStatus status) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
    }

    // Getters and setters
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
} 
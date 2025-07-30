package com.virtualbank.accountservice.dto;

import com.virtualbank.accountservice.model.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountRequestDto {
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Account type is required")
    private AccountType accountType;
    
    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Initial balance must be greater than 0")
    private BigDecimal initialBalance;

    // Getters and setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }
    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }
} 
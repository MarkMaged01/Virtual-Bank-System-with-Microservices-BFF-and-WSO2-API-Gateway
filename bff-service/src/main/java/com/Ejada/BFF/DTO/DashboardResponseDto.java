package com.Ejada.BFF.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DashboardResponseDto {
    private UUID userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private List<Account> accounts;

    public static class Account {
        private UUID accountId;
        private String accountNumber;
        private String accountType;
        private BigDecimal balance;
        private List<Transaction> transactions;

        public static class Transaction {
            private UUID transactionId;
            private BigDecimal amount;
            private UUID toAccountId;
            private String description;
            private LocalDateTime timestamp;

            // Getters and setters
            public UUID getTransactionId() { return transactionId; }
            public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
            public BigDecimal getAmount() { return amount; }
            public void setAmount(BigDecimal amount) { this.amount = amount; }
            public UUID getToAccountId() { return toAccountId; }
            public void setToAccountId(UUID toAccountId) { this.toAccountId = toAccountId; }
            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }
            public LocalDateTime getTimestamp() { return timestamp; }
            public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        }

        // Getters and setters
        public UUID getAccountId() { return accountId; }
        public void setAccountId(UUID accountId) { this.accountId = accountId; }
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }
        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }
        public List<Transaction> getTransactions() { return transactions; }
        public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
    }

    // Getters and setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public List<Account> getAccounts() { return accounts; }
    public void setAccounts(List<Account> accounts) { this.accounts = accounts; }
}

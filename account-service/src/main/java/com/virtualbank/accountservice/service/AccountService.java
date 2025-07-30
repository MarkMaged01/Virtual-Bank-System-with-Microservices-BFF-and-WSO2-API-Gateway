package com.virtualbank.accountservice.service;

import com.virtualbank.accountservice.dto.AccountRequestDto;
import com.virtualbank.accountservice.dto.AccountResponseDto;
import com.virtualbank.accountservice.dto.TransferRequestDto;
import com.virtualbank.accountservice.exception.AccountNotFoundException;
import com.virtualbank.accountservice.exception.InsufficientFundsException;
import com.virtualbank.accountservice.exception.UserNotFoundException;
import com.virtualbank.accountservice.model.*;
import com.virtualbank.accountservice.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private RestTemplate restTemplate;

    private static final String USER_SERVICE_URL = "http://localhost:50001/api"; // User Service runs on port 50001

    public AccountResponseDto createAccount(AccountRequestDto request) {
        // Validate account type
        if (request.getAccountType() == null) {
            throw new IllegalArgumentException("Invalid account type");
        }

        // Validate initial balance
        if (request.getInitialBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Initial balance must be greater than 0");
        }

        // Validate user exists
        if (!validateUserExists(request.getUserId())) {
            throw new UserNotFoundException("User with ID " + request.getUserId() + " does not exist");
        }

        // Generate unique account number
        String accountNumber = generateUniqueAccountNumber();

        // Create account
        Account account = new Account();
        account.setUserId(request.getUserId());
        account.setAccountNumber(accountNumber);
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getInitialBalance());
        account.setStatus(AccountStatus.ACTIVE);
        account.setLastTransactionTime(LocalDateTime.now());

        Account savedAccount = accountRepository.save(account);

        AccountResponseDto response = new AccountResponseDto(
            savedAccount.getAccountId(),
            savedAccount.getAccountNumber(),
            savedAccount.getAccountType(),
            savedAccount.getBalance(),
            savedAccount.getStatus()
        );
        response.setMessage("Account created successfully.");
        return response;
    }

    public AccountResponseDto getAccountById(UUID accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account with ID " + accountId + " not found."));

        return new AccountResponseDto(
            account.getAccountId(),
            account.getAccountNumber(),
            account.getAccountType(),
            account.getBalance(),
            account.getStatus()
        );
    }

    public List<AccountResponseDto> getAccountsByUserId(UUID userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        
        if (accounts.isEmpty()) {
            throw new AccountNotFoundException("No accounts found for user ID " + userId + ".");
        }

        return accounts.stream()
            .map(account -> new AccountResponseDto(
                account.getAccountId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getStatus()
            ))
            .toList();
    }

    @Transactional
    public String transferFunds(TransferRequestDto request) {
        // Validate accounts exist
        Account fromAccount = accountRepository.findById(request.getFromAccountId())
            .orElseThrow(() -> new AccountNotFoundException("From account not found"));

        Account toAccount = accountRepository.findById(request.getToAccountId())
            .orElseThrow(() -> new AccountNotFoundException("To account not found"));

        // Validate account status
        if (fromAccount.getStatus() != AccountStatus.ACTIVE || toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("One or both accounts are inactive");
        }

        // Validate sufficient funds
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds in from account");
        }

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than 0");
        }

        // Perform transfer
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
        
        // Update last transaction time
        LocalDateTime now = LocalDateTime.now();
        fromAccount.setLastTransactionTime(now);
        toAccount.setLastTransactionTime(now);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        return "Account updated successfully.";
    }

    private boolean validateUserExists(UUID userId) {
        try {
            // Convert UUID to String for User Service compatibility
            String userIdString = userId.toString();
            System.out.println("=== USER VALIDATION DEBUG ===");
            System.out.println("Validating user existence for ID: " + userIdString);
            System.out.println("Calling User Service URL: " + USER_SERVICE_URL + "/users/" + userIdString + "/exists");
            
            // Call User Service to validate user exists using the public endpoint
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                USER_SERVICE_URL + "/users/" + userIdString + "/exists",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
            );
            
            System.out.println("User Service response status: " + response.getStatusCode());
            System.out.println("User Service response body: " + response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                boolean exists = Boolean.TRUE.equals(body.get("exists"));
                System.out.println("User exists: " + exists);
                System.out.println("=== END USER VALIDATION DEBUG ===");
                return exists;
            }
            
            System.out.println("Invalid response from User Service");
            System.out.println("=== END USER VALIDATION DEBUG ===");
            return false;
            
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("=== USER VALIDATION ERROR ===");
            System.err.println("Error validating user existence for ID " + userId + ": " + e.getMessage());
            System.err.println("Error type: " + e.getClass().getSimpleName());
            e.printStackTrace();
            System.err.println("=== END USER VALIDATION ERROR ===");
            // If user doesn't exist or service is unavailable, return false
            return false;
        }
    }

    private String generateUniqueAccountNumber() {
        while (true) {
            String number = String.format("%010d", ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L));
            boolean exists = accountRepository.findAll().stream().anyMatch(a -> a.getAccountNumber().equals(number));
            if (!exists) {
                return number;
            }
        }
    }
} 
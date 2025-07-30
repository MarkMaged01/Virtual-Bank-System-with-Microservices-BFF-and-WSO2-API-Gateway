package com.virtualbank.accountservice.controller;

import com.virtualbank.accountservice.dto.AccountRequestDto;
import com.virtualbank.accountservice.dto.AccountResponseDto;
import com.virtualbank.accountservice.dto.TransferRequestDto;
import com.virtualbank.accountservice.service.AccountService;
import com.virtualbank.accountservice.service.LoggingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import java.util.Map;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoggingService loggingService;

    @PostMapping
    public ResponseEntity<AccountResponseDto> createAccount(@Valid @RequestBody AccountRequestDto request) {
        // Log the request
        loggingService.logRequest("AccountService", "/accounts", request);
        
        try {
            AccountResponseDto response = accountService.createAccount(request);
            
            // Log the response
            loggingService.logResponse("AccountService", "/accounts", response);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            // Log the error
            loggingService.logError("AccountService", "/accounts", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponseDto> getAccount(@PathVariable String accountId) {
        // Log the request
        loggingService.logRequest("AccountService", "/accounts/" + accountId, Map.of("accountId", accountId));
        
        try {
            UUID accId = UUID.fromString(accountId);
            AccountResponseDto response = accountService.getAccountById(accId);
            
            // Log the response
            loggingService.logResponse("AccountService", "/accounts/" + accountId, response);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the error
            loggingService.logError("AccountService", "/accounts/" + accountId, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/users/{userId}/accounts")
    public ResponseEntity<List<AccountResponseDto>> getUserAccounts(@PathVariable String userId, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Log the request
        loggingService.logRequest("AccountService", "/accounts/users/" + userId + "/accounts", Map.of("userId", userId));
        
        try {
            // Validate user exists first
            UUID uId = UUID.fromString(userId);
            
            // If authorization header is provided, validate the token
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                // Call User Service to validate token and get user info
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + token);
                
                try {
                    ResponseEntity<Map> response = restTemplate.exchange(
                        "http://localhost:50001/api/users/" + userId + "/profile",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Map.class
                    );
                    
                    if (response.getStatusCode() != HttpStatus.OK) {
                        loggingService.logError("AccountService", "/accounts/users/" + userId + "/accounts", "Unauthorized access");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                    }
                } catch (Exception e) {
                    loggingService.logError("AccountService", "/accounts/users/" + userId + "/accounts", "Token validation failed: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
            }
            
            List<AccountResponseDto> accounts = accountService.getAccountsByUserId(uId);
            
            // Log the response
            loggingService.logResponse("AccountService", "/accounts/users/" + userId + "/accounts", accounts);
            
            return ResponseEntity.ok(accounts);
        } catch (IllegalArgumentException e) {
            loggingService.logError("AccountService", "/accounts/users/" + userId + "/accounts", "Invalid UUID: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            loggingService.logError("AccountService", "/accounts/users/" + userId + "/accounts", e.getMessage());
            throw e;
        }
    }

    @PutMapping("/transfer")
    public ResponseEntity<Object> transferFunds(@Valid @RequestBody TransferRequestDto request) {
        // Log the request
        loggingService.logRequest("AccountService", "/accounts/transfer", request);
        
        try {
            String message = accountService.transferFunds(request);
            TransferResponse response = new TransferResponse(message);
            
            // Log the response
            loggingService.logResponse("AccountService", "/accounts/transfer", response);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the error
            loggingService.logError("AccountService", "/accounts/transfer", e.getMessage());
            throw e;
        }
    }

    // Helper class for transfer response
    private static class TransferResponse {
        private String message;

        public TransferResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
} 
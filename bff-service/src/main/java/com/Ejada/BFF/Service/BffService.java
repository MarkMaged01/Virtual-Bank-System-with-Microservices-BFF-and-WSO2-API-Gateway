package com.Ejada.BFF.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.Ejada.BFF.Config.JwtUtil;
import com.Ejada.BFF.DTO.DashboardResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class BffService {
    private static final Logger logger = LoggerFactory.getLogger(BffService.class);

    @Autowired
    private WebClient userServiceWebClient;

    @Autowired
    private WebClient accountServiceWebClient;

    @Autowired
    private WebClient transactionServiceWebClient;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public Mono<DashboardResponseDto> getDashboardData(UUID userId, String token) {
        logger.debug("Fetching dashboard data for userId: {}", userId);
        // Validate JWT token
        if (!jwtUtil.isValidJwtStructure(token)) {
            logger.error("Invalid JWT structure for userId: {}", userId);
            return Mono.error(new IllegalArgumentException("Invalid JWT token structure"));
        }

        // Fetch user profile
        Mono<DashboardResponseDto> userMono = userServiceWebClient.get()
                .uri("/api/users/{userId}/profile", userId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                        Mono.error(new IllegalArgumentException("User not found: " + userId)))
                .bodyToMono(UserResponse.class)
                .map(user -> {
                    DashboardResponseDto dto = new DashboardResponseDto();
                    dto.setUserId(userId);
                    dto.setUsername(user.getUsername());
                    dto.setEmail(user.getEmail());
                    dto.setFirstName(user.getFirst_name());
                    dto.setLastName(user.getLast_name());
                    return dto;
                })
                .onErrorResume(e -> {
                    logger.warn("Failed to fetch user profile for userId {}: {}", userId, e.getMessage());
                    // Return a default response instead of throwing an error
                    DashboardResponseDto dto = new DashboardResponseDto();
                    dto.setUserId(userId);
                    dto.setUsername("Unknown");
                    dto.setEmail("Unknown");
                    dto.setFirstName("Unknown");
                    dto.setLastName("Unknown");
                    dto.setAccounts(List.of());
                    return Mono.just(dto);
                });

        // Fetch accounts
        Mono<List<AccountResponse>> accountsMono = accountServiceWebClient.get()
                .uri("/accounts/users/{userId}/accounts", userId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToFlux(AccountResponse.class)
                .collectList()
                .onErrorResume(e -> {
                    logger.warn("Failed to fetch accounts for userId {}: {}", userId, e.getMessage());
                    return Mono.just(List.<AccountResponse>of());
                });

        // Combine user and accounts
        return userMono.flatMap(dto ->
                accountsMono.flatMap(accounts -> {
                    if (accounts.isEmpty()) {
                        dto.setAccounts(List.of());
                        return Mono.just(dto);
                    }

                    // Fetch transactions for each account asynchronously
                    Flux<DashboardResponseDto.Account> accountFlux = Flux.fromIterable(accounts)
                            .flatMap(account -> transactionServiceWebClient.get()
                                    .uri("/transactions/accounts/{accountId}/transactions", account.getAccountId())
                                    .header("Authorization", "Bearer " + token)
                                    .retrieve()
                                    .bodyToFlux(TransactionResponse.class)
                                    .collectList()
                                    .map(transactions -> {
                                        DashboardResponseDto.Account accountDto = new DashboardResponseDto.Account();
                                        accountDto.setAccountId(account.getAccountId());
                                        accountDto.setAccountNumber(account.getAccountNumber());
                                        accountDto.setAccountType(account.getAccountType());
                                        accountDto.setBalance(account.getBalance());
                                        accountDto.setTransactions(transactions.stream().map(t -> {
                                            DashboardResponseDto.Account.Transaction tx = new DashboardResponseDto.Account.Transaction();
                                            tx.setTransactionId(t.getTransactionId());
                                            tx.setAmount(t.getAmount());
                                            // Map accountId to toAccountId - this represents the other account in the transaction
                                            tx.setToAccountId(t.getAccountId());
                                            tx.setDescription(t.getDescription());
                                            tx.setTimestamp(t.getTimestamp());
                                            return tx;
                                        }).toList());
                                        return accountDto;
                                    })
                                    .onErrorResume(e -> {
                                        logger.warn("Failed to fetch transactions for account {}: {}", account.getAccountId(), e.getMessage());
                                        DashboardResponseDto.Account accountDto = new DashboardResponseDto.Account();
                                        accountDto.setAccountId(account.getAccountId());
                                        accountDto.setAccountNumber(account.getAccountNumber());
                                        accountDto.setAccountType(account.getAccountType());
                                        accountDto.setBalance(account.getBalance());
                                        accountDto.setTransactions(List.of());
                                        return Mono.just(accountDto);
                                    }));

                    return accountFlux.collectList()
                            .map(accountDtos -> {
                                dto.setAccounts(accountDtos);
                                return dto;
                            });
                })
        ).doOnSuccess(dto -> {
            try {
                kafkaTemplate.send("bff.logs", "dashboard.fetched", objectMapper.writeValueAsString(dto));
                logger.debug("Published Kafka event for dashboard fetch: userId={}", userId);
            } catch (Exception e) {
                logger.error("Failed to publish Kafka event: {}", e.getMessage());
            }
        }).onErrorResume(e -> {
            logger.error("Error fetching dashboard data for userId {}: {}", userId, e.getMessage());
            // Return a default response instead of throwing an error
            DashboardResponseDto dto = new DashboardResponseDto();
            dto.setUserId(userId);
            dto.setUsername("Error");
            dto.setEmail("Error");
            dto.setFirstName("Error");
            dto.setLastName("Error");
            dto.setAccounts(List.of());
            return Mono.just(dto);
        });
    }

    // Helper classes for deserialization
    private static class UserResponse {
        private UUID userId;
        private String username;
        private String email;
        private String first_name;  // Changed from firstName to first_name to match user service
        private String last_name;   // Changed from lastName to last_name to match user service

        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirst_name() { return first_name; }
        public void setFirst_name(String first_name) { this.first_name = first_name; }
        public String getLast_name() { return last_name; }
        public void setLast_name(String last_name) { this.last_name = last_name; }
    }

    private static class AccountResponse {
        private UUID accountId;
        private String accountNumber;
        private String accountType;
        private java.math.BigDecimal balance;

        public UUID getAccountId() { return accountId; }
        public void setAccountId(UUID accountId) { this.accountId = accountId; }
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }
        public java.math.BigDecimal getBalance() { return balance; }
        public void setBalance(java.math.BigDecimal balance) { this.balance = balance; }
    }

    private static class TransactionResponse {
        private UUID transactionId;
        private java.math.BigDecimal amount;
        private UUID accountId;  // Changed from toAccountId to accountId to match transaction service
        private String description;
        private java.time.LocalDateTime timestamp;

        public UUID getTransactionId() { return transactionId; }
        public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
        public java.math.BigDecimal getAmount() { return amount; }
        public void setAmount(java.math.BigDecimal amount) { this.amount = amount; }
        public UUID getAccountId() { return accountId; }
        public void setAccountId(UUID accountId) { this.accountId = accountId; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public java.time.LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(java.time.LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}

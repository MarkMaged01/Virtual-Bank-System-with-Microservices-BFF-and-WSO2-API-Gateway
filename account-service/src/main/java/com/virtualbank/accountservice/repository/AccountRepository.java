package com.virtualbank.accountservice.repository;

import com.virtualbank.accountservice.model.Account;
import com.virtualbank.accountservice.model.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByUserId(UUID userId);
    List<Account> findByStatusAndLastTransactionTimeBefore(AccountStatus status, LocalDateTime threshold);
} 
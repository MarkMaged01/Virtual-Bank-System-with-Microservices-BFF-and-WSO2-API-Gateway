package com.virtualbank.accountservice.scheduler;

import com.virtualbank.accountservice.model.Account;
import com.virtualbank.accountservice.model.AccountStatus;
import com.virtualbank.accountservice.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AccountInactivationScheduler {

    @Autowired
    private AccountRepository accountRepository;

    @Scheduled(fixedRate = 3600000) // Run every hour (1 hour = 3600000 milliseconds)
    public void inactivateStaleAccounts() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        
        List<Account> staleAccounts = accountRepository.findByStatusAndLastTransactionTimeBefore(
            AccountStatus.ACTIVE, threshold);
        
        for (Account account : staleAccounts) {
            account.setStatus(AccountStatus.INACTIVE);
            accountRepository.save(account);
        }
        
        if (!staleAccounts.isEmpty()) {
            System.out.println("Inactivated " + staleAccounts.size() + " stale accounts");
        }
    }
} 
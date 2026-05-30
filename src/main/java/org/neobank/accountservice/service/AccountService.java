package org.neobank.accountservice.service;

import lombok.RequiredArgsConstructor;
import org.neobank.accountservice.entity.Account;
import org.neobank.accountservice.entity.AccountLimit;
import org.neobank.accountservice.enums.AccountStatus;
import org.neobank.accountservice.enums.AccountType;
import org.neobank.accountservice.event.AccountCreatedEvent;
import org.neobank.accountservice.exception.AccountAccessDeniedException;
import org.neobank.accountservice.exception.AccountNotFoundException;
import org.neobank.accountservice.publisher.AccountEventPublisher;
import org.neobank.accountservice.repository.AccountLimitRepository;
import org.neobank.accountservice.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountLimitRepository accountLimitRepository;
    private final AccountEventPublisher accountEventPublisher;

    public void createDefaultAccount(String keycloakUserId) {
        Account account = Account.builder()
                .userId(UUID.fromString(keycloakUserId))
                .iban(generateIban())
                .currency("AZN")
                .balance(BigDecimal.ZERO)
                .accountType(AccountType.CHECKING)
                .status(AccountStatus.ACTIVE)
                .build();

        accountRepository.save(account);

        AccountLimit limit = AccountLimit.builder()
                .account(account)
                .dailyLimit(new BigDecimal("5000"))
                .monthlyLimit(new BigDecimal("50000"))
                .build();

        accountLimitRepository.save(limit);
        accountEventPublisher.publishAccountCreated(
                new AccountCreatedEvent(
                        account.getId(),
                        account.getUserId(),
                        account.getIban()
                )
        );
    }

    public List<Account> getUserAccounts(UUID userId) {
        return accountRepository.findAllByUserId(userId);
    }

    private String generateIban() {
        return "AZ" + System.currentTimeMillis();
    }

    public BigDecimal getBalance(UUID id, UUID userId) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException("Account not found"));

        if (!account.getUserId().equals(userId)) {
            throw new AccountAccessDeniedException("Account not found for this user");
        }

        return account.getBalance();
    }

    public List<String> getStatement(UUID id, UUID userId) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        if (!account.getUserId().equals(userId)) {
            throw new AccountAccessDeniedException("Access denied");
        }

        return List.of("No transactions yet");
    }
}

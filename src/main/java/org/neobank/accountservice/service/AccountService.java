package org.neobank.accountservice.service;

import lombok.RequiredArgsConstructor;
import org.neobank.accountservice.entity.Account;
import org.neobank.accountservice.entity.AccountLimit;
import org.neobank.accountservice.enums.AccountStatus;
import org.neobank.accountservice.enums.AccountType;
import org.neobank.accountservice.dto.BalanceOperationResponse;
import org.neobank.accountservice.event.AccountBalanceUpdatedEvent;
import org.neobank.accountservice.event.AccountCreatedEvent;
import org.neobank.accountservice.exception.AccountAccessDeniedException;
import org.neobank.accountservice.exception.AccountNotFoundException;
import org.neobank.accountservice.exception.InsufficientFundsException;
import org.neobank.accountservice.exception.InvalidAccountStateException;
import org.neobank.accountservice.publisher.AccountEventPublisher;
import org.neobank.accountservice.repository.AccountLimitRepository;
import org.neobank.accountservice.repository.AccountRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountLimitRepository accountLimitRepository;
    private final AccountEventPublisher accountEventPublisher;
    private final org.neobank.accountservice.client.TransactionServiceClient transactionServiceClient;

    @Transactional
    @CacheEvict(value = "user-accounts", key = "#keycloakUserId")
    public void createDefaultAccount(String keycloakUserId) {
        UUID userId = UUID.fromString(keycloakUserId);
        if (accountRepository.findByUserIdAndAccountType(userId, AccountType.CHECKING).isPresent()) {
            return;
        }

        Account account = Account.builder()
                .userId(userId)
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

    @Cacheable(value = "user-accounts", key = "#userId.toString()")
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

    public List<Object> getStatement(UUID id, UUID userId) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        if (!account.getUserId().equals(userId)) {
            throw new AccountAccessDeniedException("Access denied");
        }

        return transactionServiceClient.getTransactionsByAccount(id);
    }

    public Account getAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    public Account getCheckingAccountForUser(UUID userId) {
        return accountRepository.findByUserIdAndAccountType(userId, AccountType.CHECKING)
                .orElseThrow(() -> new AccountNotFoundException("Checking account not found for user"));
    }

    public boolean isAccountOwnedBy(UUID accountId, UUID userId) {
        return accountRepository.findById(accountId)
                .map(account -> account.getUserId().equals(userId))
                .orElse(false);
    }

    @Transactional
    @CacheEvict(value = "user-accounts", key = "#result.userId().toString()")
    public BalanceOperationResponse debit(UUID accountId, BigDecimal amount, String currency) {
        Account account = getActiveAccount(accountId);
        validateCurrency(account, currency);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds on account " + accountId);
        }
        account.setBalance(account.getBalance().subtract(amount));
        Account saved = accountRepository.save(account);
        publishBalanceUpdated(saved);
        return toBalanceResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "user-accounts", key = "#result.userId().toString()")
    public BalanceOperationResponse credit(UUID accountId, BigDecimal amount, String currency) {
        Account account = getActiveAccount(accountId);
        validateCurrency(account, currency);
        account.setBalance(account.getBalance().add(amount));
        Account saved = accountRepository.save(account);
        publishBalanceUpdated(saved);
        return toBalanceResponse(saved);
    }

    private Account getActiveAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidAccountStateException("Account is not active: " + accountId);
        }
        return account;
    }

    private void validateCurrency(Account account, String currency) {
        if (!account.getCurrency().equalsIgnoreCase(currency)) {
            throw new InvalidAccountStateException(
                    "Currency mismatch: account=" + account.getCurrency() + ", requested=" + currency);
        }
    }

    private void publishBalanceUpdated(Account account) {
        accountEventPublisher.publishBalanceUpdated(new AccountBalanceUpdatedEvent(
                account.getId(),
                account.getUserId(),
                account.getBalance(),
                account.getCurrency()
        ));
    }

    private BalanceOperationResponse toBalanceResponse(Account account) {
        return new BalanceOperationResponse(
                account.getId(),
                account.getUserId(),
                account.getBalance(),
                account.getCurrency()
        );
    }
}

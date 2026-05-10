package org.neobank.accountservice.repository;

import org.neobank.accountservice.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.neobank.accountservice.enums.AccountType;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByIban(String iban);
    List<Account> findAllByUserId(UUID userId);
    Optional<Account> findByUserIdAndAccountType(UUID userId, AccountType accountType);
}

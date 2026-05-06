package org.neobank.accountservice.repository;

import org.neobank.accountservice.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByIban(String iban);
    List<Account> findAllByUserId(UUID userId);
}

package org.neobank.accountservice.repository;

import org.neobank.accountservice.entity.AccountLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountLimitRepository extends JpaRepository<AccountLimit, UUID> {
    Optional<AccountLimit> findByAccountId(UUID accountId);
}

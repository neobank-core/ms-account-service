package org.neobank.accountservice.mapper;

import org.neobank.accountservice.dto.AccountResponse;
import org.neobank.accountservice.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getIban(),
                account.getBalance(),
                account.getCurrency()
        );
    }
}

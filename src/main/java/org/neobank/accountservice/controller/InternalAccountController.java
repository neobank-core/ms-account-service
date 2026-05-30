package org.neobank.accountservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.neobank.accountservice.dto.BalanceAdjustmentRequest;
import org.neobank.accountservice.dto.BalanceOperationResponse;
import org.neobank.accountservice.entity.Account;
import org.neobank.accountservice.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/accounts")
@RequiredArgsConstructor
public class InternalAccountController {

    private final AccountService accountService;

    @GetMapping("/{id}")
    public ResponseEntity<BalanceOperationResponse> getAccount(@PathVariable UUID id) {
        Account account = accountService.getAccount(id);
        return ResponseEntity.ok(new BalanceOperationResponse(
                account.getId(),
                account.getUserId(),
                account.getBalance(),
                account.getCurrency()
        ));
    }

    @GetMapping("/{id}/owned-by/{userId}")
    public ResponseEntity<Boolean> isOwnedBy(@PathVariable UUID id, @PathVariable UUID userId) {
        return ResponseEntity.ok(accountService.isAccountOwnedBy(id, userId));
    }

    @GetMapping("/user/{userId}/checking")
    public ResponseEntity<BalanceOperationResponse> getCheckingAccount(@PathVariable UUID userId) {
        Account account = accountService.getCheckingAccountForUser(userId);
        return ResponseEntity.ok(new BalanceOperationResponse(
                account.getId(),
                account.getUserId(),
                account.getBalance(),
                account.getCurrency()
        ));
    }

    @PostMapping("/{id}/debit")
    public ResponseEntity<BalanceOperationResponse> debit(
            @PathVariable UUID id,
            @Valid @RequestBody BalanceAdjustmentRequest request) {
        return ResponseEntity.ok(accountService.debit(id, request.amount(), request.currency()));
    }

    @PostMapping("/{id}/credit")
    public ResponseEntity<BalanceOperationResponse> credit(
            @PathVariable UUID id,
            @Valid @RequestBody BalanceAdjustmentRequest request) {
        return ResponseEntity.ok(accountService.credit(id, request.amount(), request.currency()));
    }
}

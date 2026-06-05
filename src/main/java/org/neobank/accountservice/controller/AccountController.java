package org.neobank.accountservice.controller;

import lombok.RequiredArgsConstructor;
import org.neobank.accountservice.dto.AccountResponse;
import org.neobank.accountservice.entity.Account;
import org.neobank.accountservice.mapper.AccountMapper;
import org.neobank.accountservice.service.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public ResponseEntity<List<AccountResponse>> getMyAccounts(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        List<AccountResponse> response = accountService.getUserAccounts(userId)
                .stream()
                .map(accountMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(accountService.getBalance(id, userId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/statement")
    public ResponseEntity<List<Object>> getStatement(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        List<Object> statement = accountService.getStatement(id, userId);
        return ResponseEntity.ok(statement);
    }
}

package org.neobank.accountservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

import org.neobank.accountservice.config.FeignInternalConfig;

// Пока мы не внедрили Consul, ходим по прямому имени из docker-compose (или localhost:8083, если локально)
@FeignClient(name = "transaction-service", url = "${transaction-service.url:http://localhost:8083}", configuration = FeignInternalConfig.class)
public interface TransactionServiceClient {

    @GetMapping("/api/transactions/internal/account/{accountId}")
    List<Object> getTransactionsByAccount(@PathVariable("accountId") UUID accountId);
}

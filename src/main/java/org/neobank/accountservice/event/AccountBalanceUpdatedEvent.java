package org.neobank.accountservice.event;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountBalanceUpdatedEvent(
        UUID accountId,
        UUID userId,
        BigDecimal balance,
        String currency
) {
}

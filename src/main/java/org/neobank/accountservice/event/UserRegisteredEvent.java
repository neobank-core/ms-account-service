package org.neobank.accountservice.event;

import java.time.LocalDateTime;

public record UserRegisteredEvent(
        Long userId,
        String username,
        String keycloakUserId,
        String email,
        String firstName,
        String lastName,
        String phone,
        LocalDateTime createdAt
) {
}
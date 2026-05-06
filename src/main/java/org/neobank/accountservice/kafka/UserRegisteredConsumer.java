package org.neobank.accountservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neobank.accountservice.event.UserRegisteredEvent;
import org.neobank.accountservice.service.AccountService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredConsumer {

    private final AccountService accountService;

    @KafkaListener(topics = "user.registered", groupId = "account-service-group")
    public void consume(UserRegisteredEvent event) {
        log.info("Received user registered event for user: {}", event.userId());
        accountService.createDefaultAccount(event.keycloakUserId());
    }
}
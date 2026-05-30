package org.neobank.accountservice.publisher;

import lombok.RequiredArgsConstructor;
import org.neobank.accountservice.event.AccountCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishAccountCreated(AccountCreatedEvent event) {
        kafkaTemplate.send(
                "account.created",
                event.accountId().toString(),
                event
        );
    }
}

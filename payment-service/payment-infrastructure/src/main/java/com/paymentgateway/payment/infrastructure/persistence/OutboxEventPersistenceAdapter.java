package com.paymentgateway.payment.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentgateway.payment.api.domain.PaymentAuthorizationRequestedEvent;
import com.paymentgateway.payment.api.port.out.SaveOutboxEventPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OutboxEventPersistenceAdapter implements SaveOutboxEventPort {

    private final OutboxEventJpaRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxEventPersistenceAdapter(OutboxEventJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(PaymentAuthorizationRequestedEvent event) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox event", e);
        }
        OutboxEventJpaEntity entity = new OutboxEventJpaEntity(
                UUID.fromString(event.eventId()),
                event.eventType(),
                AggregateType.PAYMENT,
                event.paymentId(),
                1,
                event.occurredAt(),
                payload
        );
        repository.save(entity);
    }
}

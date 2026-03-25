package com.paymentgateway.provider.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentgateway.provider.domain.ProviderActionRequired;
import com.paymentgateway.provider.domain.ProviderAuthorizationFailed;
import com.paymentgateway.provider.domain.ProviderAuthorizationSucceeded;
import com.paymentgateway.provider.port.out.SaveAuthorizationOutboxEventPort;
import org.springframework.stereotype.Component;

@Component
public class OutboxAuthorizationResultAdapter implements SaveAuthorizationOutboxEventPort {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxAuthorizationResultAdapter(OutboxEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveSucceeded(ProviderAuthorizationSucceeded event) {
        repository.save(new OutboxEventEntity(
                event.eventId(),
                ProviderAuthorizationSucceeded.EVENT_TYPE,
                event.paymentId(),
                event.occurredAt(),
                serialize(event),
                1));
    }

    @Override
    public void saveFailed(ProviderAuthorizationFailed event) {
        repository.save(new OutboxEventEntity(
                event.eventId(),
                ProviderAuthorizationFailed.EVENT_TYPE,
                event.paymentId(),
                event.occurredAt(),
                serialize(event),
                1));
    }

    @Override
    public void saveActionRequired(ProviderActionRequired event) {
        repository.save(new OutboxEventEntity(
                event.eventId(),
                ProviderActionRequired.EVENT_TYPE,
                event.paymentId(),
                event.occurredAt(),
                serialize(event),
                1));
    }

    private String serialize(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize authorization result event", e);
        }
    }
}

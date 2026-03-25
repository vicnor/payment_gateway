package com.paymentgateway.payment.infrastructure.persistence;

import com.paymentgateway.payment.api.port.out.EventIdempotencyPort;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ProcessedEventPersistenceAdapter implements EventIdempotencyPort {

    private final ProcessedEventRepository repository;

    public ProcessedEventPersistenceAdapter(ProcessedEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isProcessed(String eventId) {
        return repository.existsByEventId(eventId);
    }

    @Override
    public void markProcessed(String eventId) {
        repository.save(new ProcessedEventEntity(eventId, Instant.now()));
    }
}

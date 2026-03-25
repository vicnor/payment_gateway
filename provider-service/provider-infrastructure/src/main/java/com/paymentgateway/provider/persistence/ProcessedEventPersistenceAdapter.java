package com.paymentgateway.provider.persistence;

import com.paymentgateway.provider.port.out.EventIdempotencyPort;
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
        return repository.existsById(eventId);
    }

    @Override
    public void markProcessed(String eventId) {
        repository.save(new ProcessedEventEntity(eventId, Instant.now()));
    }
}

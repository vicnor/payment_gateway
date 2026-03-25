package com.paymentgateway.payment.infrastructure.persistence;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class OutboxEventRelayTransactions {

    private final OutboxEventJpaRepository repository;

    OutboxEventRelayTransactions(OutboxEventJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void claim(List<UUID> ids, Instant claimedAt, String claimedBy) {
        repository.claim(ids, claimedAt, claimedBy);
    }

    @Transactional
    public void acknowledge(UUID eventId, Instant publishedAt) {
        repository.acknowledge(eventId, publishedAt);
    }

    @Transactional
    public int unclaim(Instant staleThreshold) {
        return repository.unclaimStale(staleThreshold);
    }
}

package com.paymentgateway.provider.persistence;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class OutboxEventRelayTransactions {

    private final OutboxEventRepository repository;

    OutboxEventRelayTransactions(OutboxEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void claim(List<String> ids, Instant claimedAt, String claimedBy) {
        repository.claim(ids, claimedAt, claimedBy);
    }

    @Transactional
    public void acknowledge(String id, Instant publishedAt) {
        repository.acknowledge(id, publishedAt);
    }

    @Transactional
    public int unclaim(Instant staleThreshold) {
        return repository.unclaimStale(staleThreshold);
    }
}

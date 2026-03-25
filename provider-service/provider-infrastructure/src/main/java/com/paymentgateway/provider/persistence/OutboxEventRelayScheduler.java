package com.paymentgateway.provider.persistence;

import com.paymentgateway.provider.kafka.KafkaOutboxEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(name = "outbox.relay.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxEventRelayScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventRelayScheduler.class);

    private final OutboxEventRepository repository;
    private final KafkaOutboxEventPublisher publisher;
    private final OutboxEventRelayTransactions transactions;
    private final int batchSize;
    private final String instanceId;
    private final Duration staleClaimAfter;

    public OutboxEventRelayScheduler(
            OutboxEventRepository repository,
            KafkaOutboxEventPublisher publisher,
            OutboxEventRelayTransactions transactions,
            @Value("${outbox.relay.batch-size:10}") int batchSize,
            @Value("${outbox.relay.instance-id}") String instanceId,
            @Value("${outbox.relay.stale-claim-after-ms:30000}") long staleClaimAfterMs) {
        this.repository = repository;
        this.publisher = publisher;
        this.transactions = transactions;
        this.batchSize = batchSize;
        this.instanceId = instanceId;
        this.staleClaimAfter = Duration.ofMillis(staleClaimAfterMs);
    }

    @Scheduled(fixedDelayString = "${outbox.relay.fixed-delay-ms:500}")
    public void relay() {
        List<String> ids = repository.findClaimable(PageRequest.of(0, batchSize))
                .stream().map(OutboxEventEntity::getId).toList();
        if (ids.isEmpty()) return;

        transactions.claim(ids, Instant.now(), instanceId);

        List<OutboxEventEntity> claimed = repository.findByClaimedByAndPublishedAtIsNull(instanceId);
        for (OutboxEventEntity event : claimed) {
            publisher.publish(event);
            transactions.acknowledge(event.getId(), Instant.now());
        }
    }

    @Scheduled(fixedDelayString = "${outbox.relay.stale-claim-after-ms:30000}")
    public void unclaimStale() {
        Instant staleThreshold = Instant.now().minus(staleClaimAfter);
        int released = transactions.unclaim(staleThreshold);
        if (released > 0) {
            log.warn("Released {} stale outbox claim(s) older than {}", released, staleThreshold);
        }
    }
}

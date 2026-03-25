package com.paymentgateway.payment.infrastructure.webhook;

import com.paymentgateway.payment.api.domain.WebhookDeliveryStatus;
import com.paymentgateway.payment.infrastructure.persistence.WebhookOutboxJpaEntity;
import com.paymentgateway.payment.infrastructure.persistence.WebhookOutboxJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class WebhookDeliveryProcessor {

    private static final Logger log = LoggerFactory.getLogger(WebhookDeliveryProcessor.class);

    private final WebhookOutboxJpaRepository repository;
    private final WebhookDeliveryHttpClient httpClient;

    public WebhookDeliveryProcessor(WebhookOutboxJpaRepository repository, WebhookDeliveryHttpClient httpClient) {
        this.repository = repository;
        this.httpClient = httpClient;
    }

    @Transactional
    public void processEntry(UUID id) {
        WebhookOutboxJpaEntity entry = repository.findById(id).orElse(null);
        if (entry == null || entry.getStatus() != WebhookDeliveryStatus.PENDING) return;

        boolean delivered = httpClient.deliver(entry.getCallbackUrl(), entry.getPayload());
        Instant now = Instant.now();
        int newAttemptCount = entry.getAttemptCount() + 1;
        entry.setAttemptCount(newAttemptCount);
        entry.setLastAttemptedAt(now);

        if (delivered) {
            entry.setStatus(WebhookDeliveryStatus.DELIVERED);
            log.info("Webhook delivered id={} url={}", id, entry.getCallbackUrl());
        } else {
            Optional<Instant> nextRetry = WebhookRetryPolicy.nextRetryAt(newAttemptCount);
            if (nextRetry.isPresent()) {
                entry.setStatus(WebhookDeliveryStatus.PENDING);
                entry.setNextRetryAt(nextRetry.get());
                log.warn("Webhook delivery failed id={} attempt={} nextRetryAt={}", id, newAttemptCount, nextRetry.get());
            } else {
                entry.setStatus(WebhookDeliveryStatus.FAILED);
                log.error("Webhook delivery permanently failed id={} after {} attempts", id, newAttemptCount);
            }
        }

        repository.save(entry);
    }
}

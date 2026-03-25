package com.paymentgateway.payment.infrastructure.webhook;

import com.paymentgateway.payment.api.domain.WebhookDeliveryStatus;
import com.paymentgateway.payment.infrastructure.persistence.WebhookOutboxJpaEntity;
import com.paymentgateway.payment.infrastructure.persistence.WebhookOutboxJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(name = "webhook.scheduler.enabled", matchIfMissing = true)
public class WebhookCallbackScheduler {

    private static final Logger log = LoggerFactory.getLogger(WebhookCallbackScheduler.class);

    private final WebhookOutboxJpaRepository repository;
    private final WebhookDeliveryProcessor processor;
    private final int batchSize;

    public WebhookCallbackScheduler(WebhookOutboxJpaRepository repository,
                                    WebhookDeliveryProcessor processor,
                                    @Value("${webhook.scheduler.batch-size:20}") int batchSize) {
        this.repository = repository;
        this.processor = processor;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${webhook.scheduler.fixed-delay-ms:10000}")
    public void process() {
        List<WebhookOutboxJpaEntity> due = repository.findDue(
                Instant.now(), WebhookDeliveryStatus.PENDING, PageRequest.of(0, batchSize));
        if (due.isEmpty()) return;

        log.debug("Processing {} due webhook deliveries", due.size());
        for (WebhookOutboxJpaEntity entry : due) {
            processor.processEntry(entry.getId());
        }
    }
}

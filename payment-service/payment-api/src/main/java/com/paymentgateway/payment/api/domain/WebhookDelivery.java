package com.paymentgateway.payment.api.domain;

import java.time.Instant;
import java.util.UUID;

public class WebhookDelivery {

    private final UUID id;
    private final String aggregateType;
    private final UUID aggregateId;
    private final String eventType;
    private final String merchantId;
    private final String callbackUrl;
    private final String payload;
    private final WebhookDeliveryStatus status;
    private final int attemptCount;
    private final Instant nextRetryAt;
    private final Instant lastAttemptedAt;
    private final Instant createdAt;

    public WebhookDelivery(UUID id, String aggregateType, UUID aggregateId, String eventType,
                           String merchantId, String callbackUrl, String payload,
                           WebhookDeliveryStatus status, int attemptCount,
                           Instant nextRetryAt, Instant lastAttemptedAt, Instant createdAt) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.merchantId = merchantId;
        this.callbackUrl = callbackUrl;
        this.payload = payload;
        this.status = status;
        this.attemptCount = attemptCount;
        this.nextRetryAt = nextRetryAt;
        this.lastAttemptedAt = lastAttemptedAt;
        this.createdAt = createdAt;
    }

    public static WebhookDelivery createPending(String aggregateType, UUID aggregateId, String eventType,
                                                String merchantId, String callbackUrl, String payload) {
        Instant now = Instant.now();
        return new WebhookDelivery(UUID.randomUUID(), aggregateType, aggregateId, eventType,
                merchantId, callbackUrl, payload, WebhookDeliveryStatus.PENDING, 0, now, null, now);
    }

    public UUID getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public UUID getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getMerchantId() { return merchantId; }
    public String getCallbackUrl() { return callbackUrl; }
    public String getPayload() { return payload; }
    public WebhookDeliveryStatus getStatus() { return status; }
    public int getAttemptCount() { return attemptCount; }
    public Instant getNextRetryAt() { return nextRetryAt; }
    public Instant getLastAttemptedAt() { return lastAttemptedAt; }
    public Instant getCreatedAt() { return createdAt; }
}

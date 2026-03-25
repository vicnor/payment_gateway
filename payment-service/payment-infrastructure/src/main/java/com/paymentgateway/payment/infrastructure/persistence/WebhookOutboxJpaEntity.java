package com.paymentgateway.payment.infrastructure.persistence;

import com.paymentgateway.payment.api.domain.WebhookDelivery;
import com.paymentgateway.payment.api.domain.WebhookDeliveryStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "webhook_outbox")
public class WebhookOutboxJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 50)
    private String aggregateType;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID aggregateId;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false)
    private String merchantId;

    @Column(nullable = false, length = 2048)
    private String callbackUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WebhookDeliveryStatus status;

    @Column(nullable = false)
    private int attemptCount;

    @Column(nullable = false)
    private Instant nextRetryAt;

    private Instant lastAttemptedAt;

    @Column(nullable = false)
    private Instant createdAt;

    protected WebhookOutboxJpaEntity() {
    }

    public static WebhookOutboxJpaEntity fromDomain(WebhookDelivery delivery) {
        WebhookOutboxJpaEntity entity = new WebhookOutboxJpaEntity();
        entity.id = delivery.getId();
        entity.aggregateType = delivery.getAggregateType();
        entity.aggregateId = delivery.getAggregateId();
        entity.eventType = delivery.getEventType();
        entity.merchantId = delivery.getMerchantId();
        entity.callbackUrl = delivery.getCallbackUrl();
        entity.payload = delivery.getPayload();
        entity.status = delivery.getStatus();
        entity.attemptCount = delivery.getAttemptCount();
        entity.nextRetryAt = delivery.getNextRetryAt();
        entity.lastAttemptedAt = delivery.getLastAttemptedAt();
        entity.createdAt = delivery.getCreatedAt();
        return entity;
    }

    public WebhookDelivery toDomain() {
        return new WebhookDelivery(id, aggregateType, aggregateId, eventType, merchantId,
                callbackUrl, payload, status, attemptCount, nextRetryAt, lastAttemptedAt, createdAt);
    }

    public UUID getId() { return id; }
    public String getCallbackUrl() { return callbackUrl; }
    public String getPayload() { return payload; }
    public WebhookDeliveryStatus getStatus() { return status; }
    public int getAttemptCount() { return attemptCount; }

    public void setStatus(WebhookDeliveryStatus status) { this.status = status; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public void setNextRetryAt(Instant nextRetryAt) { this.nextRetryAt = nextRetryAt; }
    public void setLastAttemptedAt(Instant lastAttemptedAt) { this.lastAttemptedAt = lastAttemptedAt; }
}

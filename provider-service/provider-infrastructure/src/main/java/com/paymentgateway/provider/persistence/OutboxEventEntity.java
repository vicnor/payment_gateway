package com.paymentgateway.provider.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String paymentId;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private int eventVersion;

    @Column
    private Instant claimedAt;

    @Column
    private String claimedBy;

    @Column
    private Instant publishedAt;

    protected OutboxEventEntity() {
    }

    public OutboxEventEntity(String id, String eventType, String paymentId,
                             Instant occurredAt, String payload, int eventVersion) {
        this.id = id;
        this.eventType = eventType;
        this.paymentId = paymentId;
        this.occurredAt = occurredAt;
        this.payload = payload;
        this.eventVersion = eventVersion;
    }

    public String getId() { return id; }
    public String getEventType() { return eventType; }
    public String getPaymentId() { return paymentId; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getPayload() { return payload; }
    public int getEventVersion() { return eventVersion; }
    public Instant getClaimedAt() { return claimedAt; }
    public String getClaimedBy() { return claimedBy; }
    public Instant getPublishedAt() { return publishedAt; }

    public void setClaimedAt(Instant claimedAt) { this.claimedAt = claimedAt; }
    public void setClaimedBy(String claimedBy) { this.claimedBy = claimedBy; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
}

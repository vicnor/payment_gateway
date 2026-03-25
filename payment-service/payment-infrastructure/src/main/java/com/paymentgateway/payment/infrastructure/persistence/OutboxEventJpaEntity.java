package com.paymentgateway.payment.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEventJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID eventId;

    @Column(nullable = false)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AggregateType aggregateType;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID aggregateId;

    @Column(nullable = false)
    private int eventVersion;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = true)
    private Instant claimedAt;

    @Column(nullable = true)
    private String claimedBy;

    @Column(nullable = true)
    private Instant publishedAt;

    protected OutboxEventJpaEntity() {
    }

    public OutboxEventJpaEntity(UUID eventId, String eventType, AggregateType aggregateType,
                                UUID aggregateId, int eventVersion, Instant occurredAt, String payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventVersion = eventVersion;
        this.occurredAt = occurredAt;
        this.payload = payload;
    }

    public UUID getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public AggregateType getAggregateType() { return aggregateType; }
    public UUID getAggregateId() { return aggregateId; }
    public int getEventVersion() { return eventVersion; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getPayload() { return payload; }
    public Instant getClaimedAt() { return claimedAt; }
    public String getClaimedBy() { return claimedBy; }
    public Instant getPublishedAt() { return publishedAt; }

    public void setClaimedAt(Instant claimedAt) { this.claimedAt = claimedAt; }
    public void setClaimedBy(String claimedBy) { this.claimedBy = claimedBy; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
}

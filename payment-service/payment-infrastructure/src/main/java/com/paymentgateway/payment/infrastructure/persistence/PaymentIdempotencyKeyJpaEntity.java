package com.paymentgateway.payment.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_idempotency_keys")
public class PaymentIdempotencyKeyJpaEntity {

    @Id
    @Column(name = "idempotency_key", length = 36)
    private String idempotencyKey;

    @Column(name = "payment_id", nullable = false, columnDefinition = "uuid")
    private UUID paymentId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PaymentIdempotencyKeyJpaEntity() {
    }

    public PaymentIdempotencyKeyJpaEntity(String idempotencyKey, UUID paymentId, Instant createdAt) {
        this.idempotencyKey = idempotencyKey;
        this.paymentId = paymentId;
        this.createdAt = createdAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

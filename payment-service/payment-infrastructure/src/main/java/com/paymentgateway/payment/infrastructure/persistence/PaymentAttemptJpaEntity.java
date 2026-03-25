package com.paymentgateway.payment.infrastructure.persistence;

import com.paymentgateway.payment.api.domain.AttemptStatus;
import com.paymentgateway.payment.api.domain.PaymentAttempt;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_attempts")
public class PaymentAttemptJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID attemptId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID paymentId;

    @Column(nullable = false)
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status;

    @Column(nullable = true)
    private String providerPaymentRef;

    @Column(nullable = true)
    private String failureCode;

    @Column(nullable = true)
    private String failureReason;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String requiresActionPayload;

    @Column(nullable = true)
    private String cardBrand;

    @Column(nullable = true)
    private String cardLast4;

    @Column(nullable = true)
    private Integer cardExpiryMonth;

    @Column(nullable = true)
    private Integer cardExpiryYear;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected PaymentAttemptJpaEntity() {
    }

    public static PaymentAttemptJpaEntity fromDomain(PaymentAttempt attempt) {
        PaymentAttemptJpaEntity entity = new PaymentAttemptJpaEntity();
        entity.attemptId = attempt.getAttemptId();
        entity.paymentId = attempt.getPaymentId();
        entity.provider = attempt.getProvider();
        entity.status = attempt.getStatus();
        entity.providerPaymentRef = attempt.getProviderPaymentRef();
        entity.failureCode = attempt.getFailureCode();
        entity.failureReason = attempt.getFailureReason();
        entity.requiresActionPayload = attempt.getRequiresActionPayload();
        entity.cardBrand = attempt.getCardBrand();
        entity.cardLast4 = attempt.getCardLast4();
        entity.cardExpiryMonth = attempt.getCardExpiryMonth();
        entity.cardExpiryYear = attempt.getCardExpiryYear();
        entity.createdAt = attempt.getCreatedAt();
        entity.updatedAt = attempt.getUpdatedAt();
        return entity;
    }

    public PaymentAttempt toDomain() {
        return new PaymentAttempt(attemptId, paymentId, provider, status,
                providerPaymentRef, failureCode, failureReason, requiresActionPayload,
                cardBrand, cardLast4, cardExpiryMonth, cardExpiryYear,
                createdAt, updatedAt);
    }
}

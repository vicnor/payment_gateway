package com.paymentgateway.payment.api.domain;

import java.time.Instant;
import java.util.UUID;

public class PaymentAttempt {

    private final UUID attemptId;
    private final UUID paymentId;
    private final String provider;
    private AttemptStatus status;
    private String providerPaymentRef;
    private String failureCode;
    private String failureReason;
    private String requiresActionPayload;
    private String cardBrand;
    private String cardLast4;
    private Integer cardExpiryMonth;
    private Integer cardExpiryYear;
    private final Instant createdAt;
    private Instant updatedAt;

    public PaymentAttempt(UUID attemptId, UUID paymentId, String provider, AttemptStatus status,
                          String providerPaymentRef, String failureCode, String failureReason,
                          String requiresActionPayload, String cardBrand, String cardLast4,
                          Integer cardExpiryMonth, Integer cardExpiryYear,
                          Instant createdAt, Instant updatedAt) {
        this.attemptId = attemptId;
        this.paymentId = paymentId;
        this.provider = provider;
        this.status = status;
        this.providerPaymentRef = providerPaymentRef;
        this.failureCode = failureCode;
        this.failureReason = failureReason;
        this.requiresActionPayload = requiresActionPayload;
        this.cardBrand = cardBrand;
        this.cardLast4 = cardLast4;
        this.cardExpiryMonth = cardExpiryMonth;
        this.cardExpiryYear = cardExpiryYear;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PaymentAttempt create(UUID paymentId, String provider) {
        Instant now = Instant.now();
        return new PaymentAttempt(UUID.randomUUID(), paymentId, provider, AttemptStatus.INITIATED,
                null, null, null, null, null, null, null, null, now, now);
    }

    public void markAuthorized(String providerPaymentRef, String brand, String last4,
                               Integer expiryMonth, Integer expiryYear) {
        this.status = AttemptStatus.AUTHORIZED;
        this.providerPaymentRef = providerPaymentRef;
        this.cardBrand = brand;
        this.cardLast4 = last4;
        this.cardExpiryMonth = expiryMonth;
        this.cardExpiryYear = expiryYear;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String failureCode, String failureReason, String brand, String last4,
                           Integer expiryMonth, Integer expiryYear) {
        this.status = AttemptStatus.FAILED;
        this.failureCode = failureCode;
        this.failureReason = failureReason;
        this.cardBrand = brand;
        this.cardLast4 = last4;
        this.cardExpiryMonth = expiryMonth;
        this.cardExpiryYear = expiryYear;
        this.updatedAt = Instant.now();
    }

    public void markRequiresAction(String requiresActionPayload) {
        this.status = AttemptStatus.REQUIRES_ACTION;
        this.requiresActionPayload = requiresActionPayload;
        this.updatedAt = Instant.now();
    }

    public UUID getAttemptId() { return attemptId; }
    public UUID getPaymentId() { return paymentId; }
    public String getProvider() { return provider; }
    public AttemptStatus getStatus() { return status; }
    public String getProviderPaymentRef() { return providerPaymentRef; }
    public String getFailureCode() { return failureCode; }
    public String getFailureReason() { return failureReason; }
    public String getRequiresActionPayload() { return requiresActionPayload; }
    public String getCardBrand() { return cardBrand; }
    public String getCardLast4() { return cardLast4; }
    public Integer getCardExpiryMonth() { return cardExpiryMonth; }
    public Integer getCardExpiryYear() { return cardExpiryYear; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

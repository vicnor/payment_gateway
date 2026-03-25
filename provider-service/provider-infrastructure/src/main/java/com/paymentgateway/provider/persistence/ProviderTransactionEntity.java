package com.paymentgateway.provider.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "provider_transactions")
public class ProviderTransactionEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Column(name = "attempt_id", nullable = false)
    private String attemptId;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "failure_code")
    private String failureCode;

    @Column(name = "provider_payment_ref")
    private String providerPaymentRef;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ProviderTransactionEntity() {}

    public ProviderTransactionEntity(String id, String paymentId, String attemptId,
                                     String provider, String status, String failureCode,
                                     String providerPaymentRef, Instant createdAt) {
        this.id = id;
        this.paymentId = paymentId;
        this.attemptId = attemptId;
        this.provider = provider;
        this.status = status;
        this.failureCode = failureCode;
        this.providerPaymentRef = providerPaymentRef;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getPaymentId() { return paymentId; }
    public String getAttemptId() { return attemptId; }
    public String getProvider() { return provider; }
    public String getStatus() { return status; }
    public String getFailureCode() { return failureCode; }
    public String getProviderPaymentRef() { return providerPaymentRef; }
    public Instant getCreatedAt() { return createdAt; }
}

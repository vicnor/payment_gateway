package com.paymentgateway.payment.infrastructure.persistence;

import com.paymentgateway.payment.api.domain.CaptureMode;
import com.paymentgateway.payment.api.domain.Money;
import com.paymentgateway.payment.api.domain.Payment;
import com.paymentgateway.payment.api.domain.PaymentStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class PaymentJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String merchantId;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaptureMode captureMode;

    @Column(nullable = false)
    private String paymentMethodType;

    @Column(nullable = false)
    private String paymentMethodRef;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = true, length = 2048)
    private String callbackUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected PaymentJpaEntity() {
    }

    public static PaymentJpaEntity fromDomain(Payment payment) {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.id = payment.getId();
        entity.merchantId = payment.getMerchantId();
        entity.orderId = payment.getOrderId();
        entity.amount = payment.getAmount();
        entity.currency = payment.getCurrency().toString();
        entity.captureMode = payment.getCaptureMode();
        entity.paymentMethodType = payment.getPaymentMethodType();
        entity.paymentMethodRef = payment.getPaymentMethodRef();
        entity.provider = payment.getProvider();
        entity.callbackUrl = payment.getCallbackUrl();
        entity.status = payment.getStatus();
        entity.createdAt = payment.getCreatedAt();
        entity.updatedAt = payment.getUpdatedAt();
        return entity;
    }

    public Payment toDomain() {
        return new Payment(id, merchantId, orderId, amount, Currency.getInstance(currency),
                captureMode, paymentMethodType, paymentMethodRef, provider, callbackUrl,
                status, createdAt, updatedAt);
    }
}

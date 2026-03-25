package com.paymentgateway.payment.api.domain;

import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

public class Payment {

    private final UUID id;
    private final String merchantId;
    private final String orderId;
    private final Integer amount;
    private final Currency currency;
    private final CaptureMode captureMode;
    private final String paymentMethodType;
    private final String paymentMethodRef;
    private final String provider;
    private final String callbackUrl;
    private PaymentStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public Payment(UUID id, String merchantId, String orderId, Integer amount, Currency currency, CaptureMode captureMode,
                   String paymentMethodType, String paymentMethodRef, String provider, String callbackUrl,
                   PaymentStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.merchantId = merchantId;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.captureMode = captureMode;
        this.paymentMethodType = paymentMethodType;
        this.paymentMethodRef = paymentMethodRef;
        this.provider = provider;
        this.callbackUrl = callbackUrl;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Payment create(String merchantId, String orderId, Integer amount, String currencyCode,
                                 CaptureMode captureMode, String paymentMethodType, String paymentMethodRef,
                                 String provider, String callbackUrl) {
        Instant now = Instant.now();
        Currency currency = Currency.getInstance(currencyCode);
        return new Payment(UUID.randomUUID(), merchantId, orderId, amount, currency, captureMode,
                paymentMethodType, paymentMethodRef, provider, callbackUrl, PaymentStatus.PROCESSING, now, now);
    }

    public void markRequiresCustomerAction() {
        this.status = PaymentStatus.REQUIRES_CUSTOMER_ACTION;
        this.updatedAt = Instant.now();
    }

    public void markAuthorized() {
        this.status = PaymentStatus.AUTHORIZED;
        this.updatedAt = Instant.now();
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getMerchantId() { return merchantId; }
    public String getOrderId() { return orderId; }
    public Integer getAmount() {
        return amount;
    }
    public Currency getCurrency() {
        return currency;
    }
    public CaptureMode getCaptureMode() { return captureMode; }
    public String getPaymentMethodType() { return paymentMethodType; }
    public String getPaymentMethodRef() { return paymentMethodRef; }
    public String getProvider() { return provider; }
    public String getCallbackUrl() { return callbackUrl; }
    public PaymentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

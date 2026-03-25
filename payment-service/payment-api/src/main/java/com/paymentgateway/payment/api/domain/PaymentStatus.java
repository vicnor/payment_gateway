package com.paymentgateway.payment.api.domain;

public enum PaymentStatus {
    PROCESSING,
    REQUIRES_CUSTOMER_ACTION,
    AUTHORIZED,
    FAILED
}

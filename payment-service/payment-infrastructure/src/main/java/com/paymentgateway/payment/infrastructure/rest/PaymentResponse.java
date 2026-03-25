package com.paymentgateway.payment.infrastructure.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paymentgateway.payment.api.domain.Payment;
import com.paymentgateway.payment.api.domain.PaymentAttempt;

import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        String merchantId,
        String orderId,
        Integer amount,
        String currencyCode,
        String captureMode,
        PaymentMethodDetails paymentMethod,
        String provider,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PaymentMethodDetails(
            String type,
            String brand,
            String last4,
            Integer expiryMonth,
            Integer expiryYear
    ) {}

    public static PaymentResponse from(Payment payment, PaymentAttempt attempt) {
        PaymentMethodDetails pm = buildPaymentMethod(payment, attempt);
        return new PaymentResponse(
                payment.getId(),
                payment.getMerchantId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getCurrency().toString(),
                payment.getCaptureMode().name(),
                pm,
                payment.getProvider(),
                payment.getStatus().name(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    private static PaymentMethodDetails buildPaymentMethod(Payment payment, PaymentAttempt attempt) {
        String brand = attempt != null ? attempt.getCardBrand() : null;
        String last4 = attempt != null ? attempt.getCardLast4() : null;
        Integer expiryMonth = attempt != null ? attempt.getCardExpiryMonth() : null;
        Integer expiryYear = attempt != null ? attempt.getCardExpiryYear() : null;
        return new PaymentMethodDetails(payment.getPaymentMethodType(), brand, last4, expiryMonth, expiryYear);
    }
}

package com.paymentgateway.provider.domain;

import java.time.Instant;

public record PaymentAuthorizationEvent(
        String eventId,
        String eventType,
        Instant occurredAt,
        String paymentId,
        String attemptId,
        String provider,
        Integer amount,
        String currency,
        String paymentMethodType,
        String paymentMethodRef
) {}

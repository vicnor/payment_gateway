package com.paymentgateway.payment.api.domain;

import java.time.Instant;
import java.util.UUID;

public record PaymentAuthorizationRequestedEvent(
        String eventId,
        String eventType,
        Instant occurredAt,
        UUID paymentId,
        UUID attemptId,
        String provider,
        Integer amount,
        String currency,
        String paymentMethodType,
        String paymentMethodRef
) {
}

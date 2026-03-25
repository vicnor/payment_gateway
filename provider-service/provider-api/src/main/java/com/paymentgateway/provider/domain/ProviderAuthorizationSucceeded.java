package com.paymentgateway.provider.domain;

import java.time.Instant;

public record ProviderAuthorizationSucceeded(
        String eventId,
        Instant occurredAt,
        String paymentId,
        String attemptId,
        String provider,
        String providerPaymentRef,
        CardDetails cardDetails) {

    public static final String EVENT_TYPE = "ProviderAuthorizationSucceeded";
}

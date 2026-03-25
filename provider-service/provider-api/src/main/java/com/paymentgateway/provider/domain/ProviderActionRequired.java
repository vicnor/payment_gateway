package com.paymentgateway.provider.domain;

import java.time.Instant;

public record ProviderActionRequired(
        String eventId,
        Instant occurredAt,
        String paymentId,
        String attemptId,
        String provider,
        String actionPayload) {

    public static final String EVENT_TYPE = "ProviderActionRequired";
}

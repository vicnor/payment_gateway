package com.paymentgateway.provider.domain;

import java.time.Instant;

public record ProviderAuthorizationFailed(
        String eventId,
        Instant occurredAt,
        String paymentId,
        String attemptId,
        String provider,
        AuthorizationCode failureCode,
        String failureReason,
        CardDetails cardDetails) {

    public static final String EVENT_TYPE = "ProviderAuthorizationFailed";
}

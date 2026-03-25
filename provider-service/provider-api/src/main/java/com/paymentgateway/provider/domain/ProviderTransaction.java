package com.paymentgateway.provider.domain;

public record ProviderTransaction(
        String id,
        String paymentId,
        String attemptId,
        String provider,
        AuthorizationStatus status,
        String failureCode,
        String providerPaymentRef
) {}

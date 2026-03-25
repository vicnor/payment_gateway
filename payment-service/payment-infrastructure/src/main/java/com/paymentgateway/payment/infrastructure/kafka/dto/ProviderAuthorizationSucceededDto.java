package com.paymentgateway.payment.infrastructure.kafka.dto;

public record ProviderAuthorizationSucceededDto(String paymentId, String attemptId,
                                                String providerPaymentRef, CardDetailsDto cardDetails) {}

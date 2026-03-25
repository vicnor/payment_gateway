package com.paymentgateway.payment.infrastructure.kafka.dto;

public record ProviderAuthorizationFailedDto(String paymentId, String attemptId,
                                             String failureCode, String failureReason,
                                             CardDetailsDto cardDetails) {}

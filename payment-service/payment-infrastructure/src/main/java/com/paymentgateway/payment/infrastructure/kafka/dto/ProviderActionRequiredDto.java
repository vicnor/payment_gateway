package com.paymentgateway.payment.infrastructure.kafka.dto;

public record ProviderActionRequiredDto(String paymentId, String attemptId, String actionPayload) {}

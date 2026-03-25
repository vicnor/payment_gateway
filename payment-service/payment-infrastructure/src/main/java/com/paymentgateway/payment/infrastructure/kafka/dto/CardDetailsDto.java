package com.paymentgateway.payment.infrastructure.kafka.dto;

public record CardDetailsDto(String brand, String last4, int expiryMonth, int expiryYear) {}

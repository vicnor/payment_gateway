package com.paymentgateway.provider.domain;

public record CardDetails(String brand, String last4, int expiryMonth, int expiryYear) {}

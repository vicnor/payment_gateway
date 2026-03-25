package com.paymentgateway.provider.port.out;

public interface EventIdempotencyPort {
    boolean isProcessed(String eventId);
    void markProcessed(String eventId);
}

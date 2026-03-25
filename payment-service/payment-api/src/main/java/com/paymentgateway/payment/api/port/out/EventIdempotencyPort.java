package com.paymentgateway.payment.api.port.out;

public interface EventIdempotencyPort {
    boolean isProcessed(String eventId);
    void markProcessed(String eventId);
}

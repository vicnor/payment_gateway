package com.paymentgateway.payment.infrastructure.rest;

public class InvalidIdempotencyKeyException extends RuntimeException {

    public InvalidIdempotencyKeyException(String key) {
        super("Idempotency-Key is not a valid UUID: " + key);
    }
}

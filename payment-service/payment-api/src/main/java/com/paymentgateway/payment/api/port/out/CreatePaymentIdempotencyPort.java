package com.paymentgateway.payment.api.port.out;

import com.paymentgateway.payment.api.domain.Payment;

import java.util.Optional;
import java.util.UUID;

public interface CreatePaymentIdempotencyPort {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    void save(String idempotencyKey, UUID paymentId);
}

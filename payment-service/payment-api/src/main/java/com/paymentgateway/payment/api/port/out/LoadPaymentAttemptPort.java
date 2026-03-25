package com.paymentgateway.payment.api.port.out;

import com.paymentgateway.payment.api.domain.PaymentAttempt;

import java.util.Optional;
import java.util.UUID;

public interface LoadPaymentAttemptPort {
    Optional<PaymentAttempt> findByAttemptId(UUID attemptId);
    Optional<PaymentAttempt> findLatestByPaymentId(UUID paymentId);
}

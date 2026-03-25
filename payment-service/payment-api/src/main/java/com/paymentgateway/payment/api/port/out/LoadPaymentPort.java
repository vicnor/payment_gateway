package com.paymentgateway.payment.api.port.out;

import com.paymentgateway.payment.api.domain.Payment;

import java.util.Optional;
import java.util.UUID;

public interface LoadPaymentPort {

    Optional<Payment> findById(UUID paymentId);
}

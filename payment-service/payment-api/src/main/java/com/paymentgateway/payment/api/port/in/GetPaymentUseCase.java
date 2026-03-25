package com.paymentgateway.payment.api.port.in;

import com.paymentgateway.payment.api.domain.Payment;
import com.paymentgateway.payment.api.domain.PaymentAttempt;

import java.util.UUID;

public interface GetPaymentUseCase {

    record PaymentView(Payment payment, PaymentAttempt latestAttempt) {}

    PaymentView getPayment(UUID paymentId);
}

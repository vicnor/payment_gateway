package com.paymentgateway.payment.api.port.out;

import com.paymentgateway.payment.api.domain.PaymentAttempt;

public interface SavePaymentAttemptPort {

    PaymentAttempt save(PaymentAttempt attempt);
}

package com.paymentgateway.payment.api.port.out;

import com.paymentgateway.payment.api.domain.Payment;

public interface SavePaymentPort {

    Payment save(Payment payment);
}

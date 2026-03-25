package com.paymentgateway.payment.api.port.out;

import com.paymentgateway.payment.api.domain.PaymentAuthorizationRequestedEvent;

public interface SaveOutboxEventPort {

    void save(PaymentAuthorizationRequestedEvent event);
}

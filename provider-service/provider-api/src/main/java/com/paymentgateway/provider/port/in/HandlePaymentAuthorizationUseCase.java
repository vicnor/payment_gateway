package com.paymentgateway.provider.port.in;

import com.paymentgateway.provider.domain.PaymentAuthorizationEvent;

public interface HandlePaymentAuthorizationUseCase {
    void authorize(PaymentAuthorizationEvent event);
}

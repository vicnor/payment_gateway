package com.paymentgateway.provider.port.out;

import com.paymentgateway.provider.domain.AuthorizationResult;
import com.paymentgateway.provider.domain.PaymentAuthorizationEvent;

public interface ProviderGatewayPort {
    AuthorizationResult authorize(PaymentAuthorizationEvent event);
    boolean supports(String provider);
}

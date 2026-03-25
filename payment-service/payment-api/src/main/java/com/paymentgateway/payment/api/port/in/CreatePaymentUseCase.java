package com.paymentgateway.payment.api.port.in;

import com.paymentgateway.payment.api.domain.CaptureMode;
import com.paymentgateway.payment.api.domain.Payment;

public interface CreatePaymentUseCase {

    Payment createPayment(CreatePaymentCommand command);

    record CreatePaymentCommand(
            String merchantId,
            String orderId,
            Integer amount,
            String currencyCode,
            CaptureMode captureMode,
            String paymentMethodType,
            String paymentMethodRef,
            String provider,
            String idempotencyKey,
            String callbackUrl
    ) {
    }
}

package com.paymentgateway.payment.infrastructure.rest;

import com.paymentgateway.payment.api.domain.CaptureMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CreatePaymentRequest(
        @NotBlank String merchantId,
        @NotBlank String orderId,
        @NotNull @Positive Integer amount,
        @NotBlank String currencyCode,
        @NotNull CaptureMode captureMode,
        @NotBlank String provider,
        @NotNull @Valid PaymentMethodRequest paymentMethod,
        @Pattern(regexp = "^https?://.*") String callbackUrl
) {
    public record PaymentMethodRequest(
            @NotBlank String type,
            @NotBlank String token
    ) {
    }
}

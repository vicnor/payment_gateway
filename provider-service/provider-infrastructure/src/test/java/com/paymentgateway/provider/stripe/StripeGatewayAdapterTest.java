package com.paymentgateway.provider.stripe;

import com.paymentgateway.provider.domain.AuthorizationCode;
import com.paymentgateway.provider.domain.AuthorizationResult;
import com.paymentgateway.provider.domain.AuthorizationStatus;
import com.paymentgateway.provider.domain.PaymentAuthorizationEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StripeGatewayAdapterTest {

    private final StripeGatewayAdapter adapter = new StripeGatewayAdapter();

    @Test
    void supports_stripe() {
        assertTrue(adapter.supports("stripe"));
        assertTrue(adapter.supports("Stripe"));
        assertTrue(adapter.supports("STRIPE"));
    }

    @Test
    void supports_adyen() {
        assertFalse(adapter.supports("adyen"));
    }

    @Test
    void authorize_tok_visa_returnsAuthorized() {
        AuthorizationResult result = adapter.authorize(event("tok_visa"));
        assertEquals(AuthorizationStatus.AUTHORIZED, result.status());
        assertNull(result.code());
        assertTrue(result.providerPaymentRef().startsWith("pi_fake_"));
    }

    @Test
    void authorize_tok_mastercard_returnsAuthorized() {
        AuthorizationResult result = adapter.authorize(event("tok_mastercard"));
        assertEquals(AuthorizationStatus.AUTHORIZED, result.status());
        assertTrue(result.providerPaymentRef().startsWith("pi_fake_"));
    }

    @Test
    void authorize_tok_amex_returnsAuthorized() {
        AuthorizationResult result = adapter.authorize(event("tok_amex"));
        assertEquals(AuthorizationStatus.AUTHORIZED, result.status());
        assertTrue(result.providerPaymentRef().startsWith("pi_fake_"));
    }

    @Test
    void authorize_tok_3ds_requiresAction() {
        AuthorizationResult result = adapter.authorize(event("tok_3ds"));
        assertEquals(AuthorizationStatus.REQUIRES_ACTION, result.status());
        assertEquals(AuthorizationCode.THREE_DS_REQUIRED, result.code());
        assertNull(result.providerPaymentRef());
    }

    @Test
    void authorize_tok_fail_declines() {
        AuthorizationResult result = adapter.authorize(event("tok_fail"));
        assertEquals(AuthorizationStatus.DECLINED, result.status());
        assertEquals(AuthorizationCode.CARD_DECLINED, result.code());
        assertNull(result.providerPaymentRef());
    }

    @Test
    void authorize_tok_abc123_declines() {
        AuthorizationResult result = adapter.authorize(event("tok_abc123"));
        assertEquals(AuthorizationStatus.DECLINED, result.status());
        assertEquals(AuthorizationCode.CARD_DECLINED, result.code());
        assertNull(result.providerPaymentRef());
    }

    @Test
    void authorize_tok_insufficient_funds_declines() {
        AuthorizationResult result = adapter.authorize(event("tok_insufficient_funds"));
        assertEquals(AuthorizationStatus.DECLINED, result.status());
        assertEquals(AuthorizationCode.INSUFFICIENT_FUNDS, result.code());
        assertNull(result.providerPaymentRef());
    }

    private PaymentAuthorizationEvent event(String paymentMethodRef) {
        return new PaymentAuthorizationEvent(
                "evt-1",
                "PaymentAuthorizationRequested",
                Instant.parse("2026-01-01T00:00:00Z"),
                "pay-1",
                "att-1",
                "stripe",
                1000,
                "SEK",
                "CARD",
                paymentMethodRef
        );
    }
}

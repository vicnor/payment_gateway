package com.paymentgateway.provider.stripe;

import com.paymentgateway.provider.domain.AuthorizationCode;
import com.paymentgateway.provider.domain.AuthorizationResult;
import com.paymentgateway.provider.domain.CardDetails;
import com.paymentgateway.provider.domain.PaymentAuthorizationEvent;
import com.paymentgateway.provider.port.out.ProviderGatewayPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StripeGatewayAdapter implements ProviderGatewayPort {

    private static final Logger log = LoggerFactory.getLogger(StripeGatewayAdapter.class);

    @Override
    public AuthorizationResult authorize(PaymentAuthorizationEvent event) {
        AuthorizationResult result = simulateAuthorization(event.paymentMethodRef());
        log.info("Stripe authorize: paymentId={}, amount={} {}, ref={}, status={}, code={}",
                event.paymentId(), event.amount(), event.currency(),
                event.paymentMethodRef(), result.status(), result.code());
        return result;
    }

    private AuthorizationResult simulateAuthorization(String paymentMethodRef) {
        CardDetails card = cardDetailsFor(paymentMethodRef);
        if (paymentMethodRef == null) {
            return AuthorizationResult.declined(AuthorizationCode.CARD_DECLINED, card);
        }
        return switch (paymentMethodRef) {
            case "tok_visa", "tok_mastercard", "tok_amex" ->
                    AuthorizationResult.authorized("pi_fake_" + UUID.randomUUID().toString().replace("-", ""), card);
            case "tok_3ds"                                -> AuthorizationResult.requiresAction(AuthorizationCode.THREE_DS_REQUIRED);
            case "tok_insufficient_funds"                 -> AuthorizationResult.declined(AuthorizationCode.INSUFFICIENT_FUNDS, card);
            default                                       -> AuthorizationResult.declined(AuthorizationCode.CARD_DECLINED, card);
        };
    }

    private CardDetails cardDetailsFor(String token) {
        if (token == null) {
            return null;
        }
        return switch (token) {
            case "tok_visa"               -> new CardDetails("VISA",       "4242", 12, 2028);
            case "tok_mastercard"         -> new CardDetails("MASTERCARD", "5555", 12, 2028);
            case "tok_amex"               -> new CardDetails("AMEX",       "0005", 12, 2028);
            case "tok_insufficient_funds" -> new CardDetails("VISA",       "9995", 12, 2028);
            case "tok_3ds"                -> new CardDetails("VISA",       "3220", 12, 2028);
            default                       -> null;
        };
    }

    @Override
    public boolean supports(String provider) {
        return "stripe".equalsIgnoreCase(provider);
    }
}

package com.paymentgateway.provider.router;

import com.paymentgateway.provider.domain.AuthorizationResult;
import com.paymentgateway.provider.domain.PaymentAuthorizationEvent;
import com.paymentgateway.provider.port.out.ProviderGatewayPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ProviderRouterTest {

    @Test
    void route_delegatesToMatchingGateway() {
        ProviderGatewayPort gateway = mock(ProviderGatewayPort.class);
        PaymentAuthorizationEvent event = sampleEvent("stripe");
        given(gateway.supports("stripe")).willReturn(true);
        given(gateway.authorize(event)).willReturn(AuthorizationResult.authorized("pi_fake_ref", null));

        ProviderRouter router = new ProviderRouter(List.of(gateway));
        AuthorizationResult result = router.route(event);

        verify(gateway).authorize(event);
        assertEquals(AuthorizationResult.authorized("pi_fake_ref", null), result);
    }

    @Test
    void route_throwsForUnknownProvider() {
        ProviderGatewayPort gateway = mock(ProviderGatewayPort.class);
        given(gateway.supports("adyen")).willReturn(false);

        ProviderRouter router = new ProviderRouter(List.of(gateway));

        assertThrows(IllegalArgumentException.class, () -> router.route(sampleEvent("adyen")));
    }

    private PaymentAuthorizationEvent sampleEvent(String provider) {
        return new PaymentAuthorizationEvent(
                "evt-1",
                "PaymentAuthorizationRequested",
                Instant.parse("2026-01-01T00:00:00Z"),
                "pay-1",
                "att-1",
                provider,
                1000,
                "SEK",
                "CARD",
                "tok_visa"
        );
    }
}

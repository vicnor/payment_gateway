package com.paymentgateway.provider.router;

import com.paymentgateway.provider.domain.AuthorizationResult;
import com.paymentgateway.provider.domain.PaymentAuthorizationEvent;
import com.paymentgateway.provider.port.out.ProviderGatewayPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProviderRouter {

    private final List<ProviderGatewayPort> gateways;

    public ProviderRouter(List<ProviderGatewayPort> gateways) {
        this.gateways = gateways;
    }

    public AuthorizationResult route(PaymentAuthorizationEvent event) {
        return gateways.stream()
                .filter(g -> g.supports(event.provider()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No gateway registered for provider: " + event.provider()))
                .authorize(event);
    }
}

package com.paymentgateway.payment.infrastructure.webhook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class WebhookDeliveryHttpClient {

    private static final Logger log = LoggerFactory.getLogger(WebhookDeliveryHttpClient.class);

    private final RestClient restClient;

    public WebhookDeliveryHttpClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public boolean deliver(String callbackUrl, String payload) {
        try {
            restClient.post()
                    .uri(callbackUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientException e) {
            log.warn("Webhook delivery failed for url={}: {}", callbackUrl, e.getMessage());
            return false;
        }
    }
}

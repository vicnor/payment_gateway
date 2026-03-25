package com.paymentgateway.payment.infrastructure.persistence;

import com.paymentgateway.payment.api.domain.WebhookDelivery;
import com.paymentgateway.payment.api.port.out.SaveWebhookDeliveryPort;
import org.springframework.stereotype.Component;

@Component
public class WebhookDeliveryPersistenceAdapter implements SaveWebhookDeliveryPort {

    private final WebhookOutboxJpaRepository repository;

    public WebhookDeliveryPersistenceAdapter(WebhookOutboxJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(WebhookDelivery delivery) {
        repository.save(WebhookOutboxJpaEntity.fromDomain(delivery));
    }
}

package com.paymentgateway.payment.api.port.out;

import com.paymentgateway.payment.api.domain.WebhookDelivery;

public interface SaveWebhookDeliveryPort {

    void save(WebhookDelivery delivery);
}

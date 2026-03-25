package com.paymentgateway.payment.infrastructure.persistence;

public interface OutboxEventPublisher {

    void publish(OutboxEventJpaEntity event);
}

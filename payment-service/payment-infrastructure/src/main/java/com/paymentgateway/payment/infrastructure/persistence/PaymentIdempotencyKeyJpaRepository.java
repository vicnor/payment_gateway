package com.paymentgateway.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentIdempotencyKeyJpaRepository extends JpaRepository<PaymentIdempotencyKeyJpaEntity, String> {
}

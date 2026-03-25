package com.paymentgateway.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentAttemptJpaRepository extends JpaRepository<PaymentAttemptJpaEntity, UUID> {
    Optional<PaymentAttemptJpaEntity> findByAttemptId(UUID attemptId);
    Optional<PaymentAttemptJpaEntity> findTopByPaymentIdOrderByCreatedAtDesc(UUID paymentId);
}

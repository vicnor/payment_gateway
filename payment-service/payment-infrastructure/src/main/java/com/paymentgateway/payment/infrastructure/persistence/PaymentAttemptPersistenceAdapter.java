package com.paymentgateway.payment.infrastructure.persistence;

import com.paymentgateway.payment.api.domain.PaymentAttempt;
import com.paymentgateway.payment.api.port.out.LoadPaymentAttemptPort;
import com.paymentgateway.payment.api.port.out.SavePaymentAttemptPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentAttemptPersistenceAdapter implements SavePaymentAttemptPort, LoadPaymentAttemptPort {

    private final PaymentAttemptJpaRepository repository;

    public PaymentAttemptPersistenceAdapter(PaymentAttemptJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public PaymentAttempt save(PaymentAttempt attempt) {
        PaymentAttemptJpaEntity entity = repository.save(PaymentAttemptJpaEntity.fromDomain(attempt));
        return entity.toDomain();
    }

    @Override
    public Optional<PaymentAttempt> findByAttemptId(UUID attemptId) {
        return repository.findByAttemptId(attemptId).map(PaymentAttemptJpaEntity::toDomain);
    }

    @Override
    public Optional<PaymentAttempt> findLatestByPaymentId(UUID paymentId) {
        return repository.findTopByPaymentIdOrderByCreatedAtDesc(paymentId).map(PaymentAttemptJpaEntity::toDomain);
    }
}

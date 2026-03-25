package com.paymentgateway.payment.infrastructure.persistence;

import com.paymentgateway.payment.api.domain.Payment;
import com.paymentgateway.payment.api.port.out.LoadPaymentPort;
import com.paymentgateway.payment.api.port.out.SavePaymentPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentPersistenceAdapter implements SavePaymentPort, LoadPaymentPort {

    private final PaymentJpaRepository repository;

    public PaymentPersistenceAdapter(PaymentJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = repository.save(PaymentJpaEntity.fromDomain(payment));
        return entity.toDomain();
    }

    @Override
    public Optional<Payment> findById(UUID paymentId) {
        return repository.findById(paymentId).map(PaymentJpaEntity::toDomain);
    }
}

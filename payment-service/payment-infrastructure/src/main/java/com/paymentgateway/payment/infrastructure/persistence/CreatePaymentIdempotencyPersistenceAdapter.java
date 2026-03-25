package com.paymentgateway.payment.infrastructure.persistence;

import com.paymentgateway.payment.api.domain.Payment;
import com.paymentgateway.payment.api.port.out.CreatePaymentIdempotencyPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class CreatePaymentIdempotencyPersistenceAdapter implements CreatePaymentIdempotencyPort {

    private final PaymentIdempotencyKeyJpaRepository idempotencyKeyRepository;
    private final PaymentJpaRepository paymentRepository;

    public CreatePaymentIdempotencyPersistenceAdapter(PaymentIdempotencyKeyJpaRepository idempotencyKeyRepository,
                                                      PaymentJpaRepository paymentRepository) {
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return idempotencyKeyRepository.findById(idempotencyKey)
                .flatMap(entity -> paymentRepository.findById(entity.getPaymentId()))
                .map(PaymentJpaEntity::toDomain);
    }

    @Override
    public void save(String idempotencyKey, UUID paymentId) {
        PaymentIdempotencyKeyJpaEntity entity = new PaymentIdempotencyKeyJpaEntity(idempotencyKey, paymentId, Instant.now());
        try {
            idempotencyKeyRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw e;
        }
    }
}

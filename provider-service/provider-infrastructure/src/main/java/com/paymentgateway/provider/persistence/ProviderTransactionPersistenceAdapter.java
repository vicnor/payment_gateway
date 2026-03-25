package com.paymentgateway.provider.persistence;

import com.paymentgateway.provider.domain.ProviderTransaction;
import com.paymentgateway.provider.port.out.SaveProviderTransactionPort;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ProviderTransactionPersistenceAdapter implements SaveProviderTransactionPort {

    private final ProviderTransactionRepository repository;

    public ProviderTransactionPersistenceAdapter(ProviderTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(ProviderTransaction transaction) {
        repository.save(new ProviderTransactionEntity(
                transaction.id(),
                transaction.paymentId(),
                transaction.attemptId(),
                transaction.provider(),
                transaction.status().name(),
                transaction.failureCode(),
                transaction.providerPaymentRef(),
                Instant.now()
        ));
    }
}

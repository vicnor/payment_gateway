package com.paymentgateway.provider.persistence;

import com.paymentgateway.provider.domain.AuthorizationStatus;
import com.paymentgateway.provider.domain.ProviderTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(ProviderTransactionPersistenceAdapter.class)
class ProviderTransactionPersistenceAdapterTest {

    @Autowired
    ProviderTransactionPersistenceAdapter adapter;

    @Autowired
    ProviderTransactionRepository repository;

    @Test
    void save_authorized_persistsAllColumns() {
        ProviderTransaction tx = new ProviderTransaction(
                "tx-001", "pay-1", "att-1", "stripe",
                AuthorizationStatus.AUTHORIZED, null, "pi_fake_abc123");

        adapter.save(tx);

        Optional<ProviderTransactionEntity> found = repository.findById("tx-001");
        assertTrue(found.isPresent());
        ProviderTransactionEntity entity = found.get();
        assertEquals("tx-001", entity.getId());
        assertEquals("pay-1", entity.getPaymentId());
        assertEquals("att-1", entity.getAttemptId());
        assertEquals("stripe", entity.getProvider());
        assertEquals("AUTHORIZED", entity.getStatus());
        assertNull(entity.getFailureCode());
        assertEquals("pi_fake_abc123", entity.getProviderPaymentRef());
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    void save_declined_persistsFailureCode() {
        ProviderTransaction tx = new ProviderTransaction(
                "tx-002", "pay-2", "att-2", "stripe",
                AuthorizationStatus.DECLINED, "INSUFFICIENT_FUNDS", null);

        adapter.save(tx);

        ProviderTransactionEntity entity = repository.findById("tx-002").orElseThrow();
        assertEquals("DECLINED", entity.getStatus());
        assertEquals("INSUFFICIENT_FUNDS", entity.getFailureCode());
        assertNull(entity.getProviderPaymentRef());
    }

    @Test
    void save_requiresAction_persistsFailureCode() {
        ProviderTransaction tx = new ProviderTransaction(
                "tx-003", "pay-3", "att-3", "stripe",
                AuthorizationStatus.REQUIRES_ACTION, "THREE_DS_REQUIRED", null);

        adapter.save(tx);

        ProviderTransactionEntity entity = repository.findById("tx-003").orElseThrow();
        assertEquals("REQUIRES_ACTION", entity.getStatus());
        assertEquals("THREE_DS_REQUIRED", entity.getFailureCode());
        assertNull(entity.getProviderPaymentRef());
    }
}

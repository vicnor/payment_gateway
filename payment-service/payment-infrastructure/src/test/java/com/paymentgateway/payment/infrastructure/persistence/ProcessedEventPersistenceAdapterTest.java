package com.paymentgateway.payment.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(ProcessedEventPersistenceAdapter.class)
class ProcessedEventPersistenceAdapterTest {

    @Autowired
    ProcessedEventPersistenceAdapter adapter;

    @Test
    void isProcessed_returnsFalseForUnknownEventId() {
        assertThat(adapter.isProcessed("unknown-event-id")).isFalse();
    }

    @Test
    void markProcessed_persistsEvent_andIsProcessedReturnsTrue() {
        String eventId = "evt-123";

        adapter.markProcessed(eventId);

        assertThat(adapter.isProcessed(eventId)).isTrue();
    }
}

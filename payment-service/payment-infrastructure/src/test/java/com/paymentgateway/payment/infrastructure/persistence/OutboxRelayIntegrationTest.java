package com.paymentgateway.payment.infrastructure.persistence;

import com.paymentgateway.payment.api.domain.CaptureMode;
import com.paymentgateway.payment.api.port.in.CreatePaymentUseCase;
import com.paymentgateway.payment.api.port.in.CreatePaymentUseCase.CreatePaymentCommand;
import com.paymentgateway.payment.infrastructure.SuperTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@TestPropertySource(properties = {
        "outbox.relay.enabled=true",
        "outbox.relay.instance-id=test-instance"
})
class OutboxRelayIntegrationTest extends SuperTest {

    @MockitoBean
    OutboxEventPublisher publisher;

    @Autowired
    OutboxEventRelayScheduler scheduler;

    @Autowired
    CreatePaymentUseCase createPaymentUseCase;

    @Autowired
    OutboxEventJpaRepository outboxEventJpaRepository;

    @Test
    void relay_publishesOutboxEventAndMarksItPublished() {
        var command = new CreatePaymentCommand(
                "merchant-1", "order-1", 1000, "SEK",
                CaptureMode.IMMEDIATE, "CARD", "tok_visa", "stripe",
                java.util.UUID.randomUUID().toString());
        createPaymentUseCase.createPayment(command);

        // event exists and is unclaimed/unpublished
        assertThat(outboxEventJpaRepository.findAll())
                .anyMatch(e -> "PaymentAuthorizationRequested".equals(e.getEventType())
                               && e.getPublishedAt() == null);

        scheduler.relay();

        // publisher.publish() called with the right event
        var captor = ArgumentCaptor.forClass(OutboxEventJpaEntity.class);
        verify(publisher).publish(captor.capture());
        var captured = captor.getValue();
        assertThat(captured.getEventType()).isEqualTo("PaymentAuthorizationRequested");
        assertThat(captured.getEventVersion()).isEqualTo(1);
        assertThat(captured.getPayload()).isNotBlank();

        // event is marked published in the DB
        var afterRelay = outboxEventJpaRepository.findById(captured.getEventId()).orElseThrow();
        assertThat(afterRelay.getPublishedAt()).isNotNull();
    }
}

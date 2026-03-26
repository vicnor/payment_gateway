package com.paymentgateway.payment.application.service;

import com.paymentgateway.payment.api.domain.AttemptStatus;
import com.paymentgateway.payment.api.domain.Payment;
import com.paymentgateway.payment.api.domain.PaymentAttempt;
import com.paymentgateway.payment.api.domain.PaymentStatus;
import com.paymentgateway.payment.api.port.in.HandleAuthorizationResultUseCase.AuthorizationActionRequiredCommand;
import com.paymentgateway.payment.api.port.in.HandleAuthorizationResultUseCase.AuthorizationFailedCommand;
import com.paymentgateway.payment.api.port.in.HandleAuthorizationResultUseCase.AuthorizationSucceededCommand;
import com.paymentgateway.payment.api.port.out.CreatePaymentIdempotencyPort;
import com.paymentgateway.payment.api.port.out.EventIdempotencyPort;
import com.paymentgateway.payment.api.port.out.LoadPaymentAttemptPort;
import com.paymentgateway.payment.api.port.out.LoadPaymentPort;
import com.paymentgateway.payment.api.port.out.SaveOutboxEventPort;
import com.paymentgateway.payment.api.port.out.SavePaymentAttemptPort;
import com.paymentgateway.payment.api.port.out.SavePaymentPort;
import com.paymentgateway.payment.api.port.out.SaveWebhookDeliveryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentServiceAuthorizationTest {

    @Mock SavePaymentPort savePaymentPort;
    @Mock LoadPaymentPort loadPaymentPort;
    @Mock SavePaymentAttemptPort savePaymentAttemptPort;
    @Mock LoadPaymentAttemptPort loadPaymentAttemptPort;
    @Mock SaveOutboxEventPort saveOutboxEventPort;
    @Mock EventIdempotencyPort eventIdempotencyPort;
    @Mock CreatePaymentIdempotencyPort createPaymentIdempotencyPort;
    @Mock SaveWebhookDeliveryPort saveWebhookDeliveryPort;

    PaymentService service;

    UUID paymentId;
    UUID attemptId;
    String eventId;
    Payment payment;
    PaymentAttempt attempt;

    @BeforeEach
    void setUp() {
        service = new PaymentService(savePaymentPort, loadPaymentPort, savePaymentAttemptPort,
                loadPaymentAttemptPort, saveOutboxEventPort, eventIdempotencyPort, createPaymentIdempotencyPort,
                saveWebhookDeliveryPort);

        paymentId = UUID.randomUUID();
        attemptId = UUID.randomUUID();
        eventId = UUID.randomUUID().toString();

        payment = new Payment(paymentId, "merchant-1", "order-1", 10000,
                java.util.Currency.getInstance("SEK"),
                com.paymentgateway.payment.api.domain.CaptureMode.IMMEDIATE,
                "CARD", "card-ref-1", "stripe", null,
                PaymentStatus.PROCESSING, java.time.Instant.now(), java.time.Instant.now());

        attempt = new PaymentAttempt(attemptId, paymentId, "stripe",
                AttemptStatus.INITIATED, null, null, null, null, null, null, null, null,
                java.time.Instant.now(), java.time.Instant.now());

        when(eventIdempotencyPort.isProcessed(eventId)).thenReturn(false);
        when(loadPaymentPort.findById(paymentId)).thenReturn(Optional.of(payment));
        when(loadPaymentAttemptPort.findByAttemptId(attemptId)).thenReturn(Optional.of(attempt));
        when(savePaymentPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(savePaymentAttemptPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void handleSucceeded_marksPaymentAndAttemptAuthorized() {
        var command = new AuthorizationSucceededCommand(paymentId, attemptId, eventId, "prov-ref-123",
                null, null, null, null);

        service.handleSucceeded(command);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.AUTHORIZED);
        assertThat(attempt.getProviderPaymentRef()).isEqualTo("prov-ref-123");
        verify(savePaymentPort).save(payment);
        verify(savePaymentAttemptPort).save(attempt);
        verify(eventIdempotencyPort).markProcessed(eventId);
    }

    @Test
    void handleFailed_marksPaymentAndAttemptFailed() {
        var command = new AuthorizationFailedCommand(paymentId, attemptId, eventId, "CARD_DECLINED", "Insufficient funds",
                null, null, null, null);

        service.handleFailed(command);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.FAILED);
        assertThat(attempt.getFailureCode()).isEqualTo("CARD_DECLINED");
        assertThat(attempt.getFailureReason()).isEqualTo("Insufficient funds");
        verify(savePaymentPort).save(payment);
        verify(savePaymentAttemptPort).save(attempt);
        verify(eventIdempotencyPort).markProcessed(eventId);
    }

    @Test
    void handleActionRequired_marksPaymentAndAttemptRequiresAction() {
        var command = new AuthorizationActionRequiredCommand(paymentId, attemptId, eventId, "{\"type\":\"3ds\"}");

        service.handleActionRequired(command);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REQUIRES_CUSTOMER_ACTION);
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.REQUIRES_ACTION);
        assertThat(attempt.getRequiresActionPayload()).isEqualTo("{\"type\":\"3ds\"}");
        verify(savePaymentPort).save(payment);
        verify(savePaymentAttemptPort).save(attempt);
        verify(eventIdempotencyPort).markProcessed(eventId);
    }

    @Test
    void handleSucceeded_idempotency_skipsProcessingForDuplicateEventId() {
        when(eventIdempotencyPort.isProcessed(eventId)).thenReturn(true);
        var command = new AuthorizationSucceededCommand(paymentId, attemptId, eventId, "prov-ref-123",
                null, null, null, null);

        service.handleSucceeded(command);

        verify(loadPaymentPort, never()).findById(any());
        verify(loadPaymentAttemptPort, never()).findByAttemptId(any());
        verify(savePaymentPort, never()).save(any());
        verify(savePaymentAttemptPort, never()).save(any());
        verify(eventIdempotencyPort, never()).markProcessed(any());
    }
}

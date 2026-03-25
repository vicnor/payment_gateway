package com.paymentgateway.provider.service;

import com.paymentgateway.provider.domain.AuthorizationCode;
import com.paymentgateway.provider.domain.AuthorizationResult;
import com.paymentgateway.provider.domain.AuthorizationStatus;
import com.paymentgateway.provider.domain.PaymentAuthorizationEvent;
import com.paymentgateway.provider.domain.ProviderActionRequired;
import com.paymentgateway.provider.domain.ProviderAuthorizationFailed;
import com.paymentgateway.provider.domain.ProviderAuthorizationSucceeded;
import com.paymentgateway.provider.domain.ProviderTransaction;
import com.paymentgateway.provider.port.out.EventIdempotencyPort;
import com.paymentgateway.provider.port.out.SaveAuthorizationOutboxEventPort;
import com.paymentgateway.provider.port.out.SaveProviderTransactionPort;
import com.paymentgateway.provider.router.ProviderRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PaymentAuthorizationServiceTest {

    @Mock
    EventIdempotencyPort idempotencyPort;

    @Mock
    SaveAuthorizationOutboxEventPort outboxPort;

    @Mock
    SaveProviderTransactionPort transactionPort;

    PaymentAuthorizationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authorize_authorized_publishesSucceeded() {
        given(idempotencyPort.isProcessed("evt-1")).willReturn(false);
        service = serviceWithResult(AuthorizationResult.authorized("pi_fake_abc123", null));

        service.authorize(sampleEvent("evt-1"));

        ArgumentCaptor<ProviderAuthorizationSucceeded> captor =
                ArgumentCaptor.forClass(ProviderAuthorizationSucceeded.class);
        verify(outboxPort).saveSucceeded(captor.capture());
        ProviderAuthorizationSucceeded event = captor.getValue();
        assertEquals("pay-1", event.paymentId());
        assertEquals("att-1", event.attemptId());
        assertEquals("stripe", event.provider());
        assertEquals("pi_fake_abc123", event.providerPaymentRef());
        verify(idempotencyPort).markProcessed("evt-1");
    }

    @Test
    void authorize_authorized_savesTransaction() {
        given(idempotencyPort.isProcessed("evt-1")).willReturn(false);
        service = serviceWithResult(AuthorizationResult.authorized("pi_fake_abc123", null));

        service.authorize(sampleEvent("evt-1"));

        ArgumentCaptor<ProviderTransaction> captor = ArgumentCaptor.forClass(ProviderTransaction.class);
        verify(transactionPort).save(captor.capture());
        ProviderTransaction tx = captor.getValue();
        assertNotNull(tx.id());
        assertEquals("pay-1", tx.paymentId());
        assertEquals("att-1", tx.attemptId());
        assertEquals("stripe", tx.provider());
        assertEquals(AuthorizationStatus.AUTHORIZED, tx.status());
        assertNull(tx.failureCode());
        assertEquals("pi_fake_abc123", tx.providerPaymentRef());
    }

    @Test
    void authorize_declined_publishesFailed() {
        given(idempotencyPort.isProcessed("evt-1")).willReturn(false);
        service = serviceWithResult(AuthorizationResult.declined(AuthorizationCode.INSUFFICIENT_FUNDS, null));

        service.authorize(sampleEvent("evt-1"));

        ArgumentCaptor<ProviderAuthorizationFailed> captor =
                ArgumentCaptor.forClass(ProviderAuthorizationFailed.class);
        verify(outboxPort).saveFailed(captor.capture());
        ProviderAuthorizationFailed event = captor.getValue();
        assertEquals("pay-1", event.paymentId());
        assertEquals(AuthorizationCode.INSUFFICIENT_FUNDS, event.failureCode());
        assertEquals("Insufficient funds", event.failureReason());
    }

    @Test
    void authorize_declined_savesTransaction() {
        given(idempotencyPort.isProcessed("evt-1")).willReturn(false);
        service = serviceWithResult(AuthorizationResult.declined(AuthorizationCode.INSUFFICIENT_FUNDS, null));

        service.authorize(sampleEvent("evt-1"));

        ArgumentCaptor<ProviderTransaction> captor = ArgumentCaptor.forClass(ProviderTransaction.class);
        verify(transactionPort).save(captor.capture());
        ProviderTransaction tx = captor.getValue();
        assertEquals(AuthorizationStatus.DECLINED, tx.status());
        assertEquals("INSUFFICIENT_FUNDS", tx.failureCode());
        assertNull(tx.providerPaymentRef());
    }

    @Test
    void authorize_requiresAction_publishesActionRequired() {
        given(idempotencyPort.isProcessed("evt-1")).willReturn(false);
        service = serviceWithResult(AuthorizationResult.requiresAction(AuthorizationCode.THREE_DS_REQUIRED));

        service.authorize(sampleEvent("evt-1"));

        ArgumentCaptor<ProviderActionRequired> captor =
                ArgumentCaptor.forClass(ProviderActionRequired.class);
        verify(outboxPort).saveActionRequired(captor.capture());
        ProviderActionRequired event = captor.getValue();
        assertEquals("pay-1", event.paymentId());
        assertEquals("", event.actionPayload());
    }

    @Test
    void authorize_requiresAction_savesTransaction() {
        given(idempotencyPort.isProcessed("evt-1")).willReturn(false);
        service = serviceWithResult(AuthorizationResult.requiresAction(AuthorizationCode.THREE_DS_REQUIRED));

        service.authorize(sampleEvent("evt-1"));

        ArgumentCaptor<ProviderTransaction> captor = ArgumentCaptor.forClass(ProviderTransaction.class);
        verify(transactionPort).save(captor.capture());
        ProviderTransaction tx = captor.getValue();
        assertEquals(AuthorizationStatus.REQUIRES_ACTION, tx.status());
        assertEquals("THREE_DS_REQUIRED", tx.failureCode());
        assertNull(tx.providerPaymentRef());
    }

    @Test
    void authorize_ignoresDuplicateEvent() {
        given(idempotencyPort.isProcessed("evt-1")).willReturn(true);
        service = serviceWithResult(AuthorizationResult.authorized("pi_fake_abc123", null));

        service.authorize(sampleEvent("evt-1"));

        verify(outboxPort, never()).saveSucceeded(any());
        verify(outboxPort, never()).saveFailed(any());
        verify(outboxPort, never()).saveActionRequired(any());
        verify(transactionPort, never()).save(any());
        verify(idempotencyPort, never()).markProcessed(any());
    }

    @Test
    void authorize_txIdLinksTransactionAndPublishedEvent() {
        given(idempotencyPort.isProcessed("evt-1")).willReturn(false);
        service = serviceWithResult(AuthorizationResult.authorized("pi_fake_abc123", null));

        service.authorize(sampleEvent("evt-1"));

        ArgumentCaptor<ProviderTransaction> txCaptor = ArgumentCaptor.forClass(ProviderTransaction.class);
        verify(transactionPort).save(txCaptor.capture());
        ArgumentCaptor<ProviderAuthorizationSucceeded> evtCaptor =
                ArgumentCaptor.forClass(ProviderAuthorizationSucceeded.class);
        verify(outboxPort).saveSucceeded(evtCaptor.capture());

        assertEquals(txCaptor.getValue().id(), evtCaptor.getValue().eventId());
    }

    private PaymentAuthorizationService serviceWithResult(AuthorizationResult fixedResult) {
        ProviderRouter stubRouter = new ProviderRouter(List.of()) {
            @Override
            public AuthorizationResult route(PaymentAuthorizationEvent event) {
                return fixedResult;
            }
        };
        return new PaymentAuthorizationService(idempotencyPort, stubRouter, transactionPort, outboxPort);
    }

    private PaymentAuthorizationEvent sampleEvent(String eventId) {
        return new PaymentAuthorizationEvent(
                eventId,
                "PaymentAuthorizationRequested",
                Instant.parse("2026-01-01T00:00:00Z"),
                "pay-1",
                "att-1",
                "stripe",
                1000,
                "SEK",
                "CARD",
                "tok_visa"
        );
    }
}

package com.paymentgateway.provider.service;

import com.paymentgateway.provider.domain.AuthorizationResult;
import com.paymentgateway.provider.domain.PaymentAuthorizationEvent;
import com.paymentgateway.provider.domain.ProviderActionRequired;
import com.paymentgateway.provider.domain.ProviderAuthorizationFailed;
import com.paymentgateway.provider.domain.ProviderAuthorizationSucceeded;
import com.paymentgateway.provider.domain.ProviderTransaction;
import com.paymentgateway.provider.port.in.HandlePaymentAuthorizationUseCase;
import com.paymentgateway.provider.port.out.EventIdempotencyPort;
import com.paymentgateway.provider.port.out.SaveAuthorizationOutboxEventPort;
import com.paymentgateway.provider.port.out.SaveProviderTransactionPort;
import com.paymentgateway.provider.router.ProviderRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentAuthorizationService implements HandlePaymentAuthorizationUseCase {

    private static final Logger log = LoggerFactory.getLogger(PaymentAuthorizationService.class);

    private final EventIdempotencyPort eventIdempotencyPort;
    private final ProviderRouter providerRouter;
    private final SaveProviderTransactionPort transactionPort;
    private final SaveAuthorizationOutboxEventPort outboxPort;

    public PaymentAuthorizationService(EventIdempotencyPort eventIdempotencyPort,
                                       ProviderRouter providerRouter,
                                       SaveProviderTransactionPort transactionPort,
                                       SaveAuthorizationOutboxEventPort outboxPort) {
        this.eventIdempotencyPort = eventIdempotencyPort;
        this.providerRouter = providerRouter;
        this.transactionPort = transactionPort;
        this.outboxPort = outboxPort;
    }

    @Override
    public void authorize(PaymentAuthorizationEvent event) {
        if (eventIdempotencyPort.isProcessed(event.eventId())) {
            log.info("Event {} already processed, ignoring", event.eventId());
            return;
        }
        AuthorizationResult result = providerRouter.route(event);
        String txId = UUID.randomUUID().toString();
        transactionPort.save(new ProviderTransaction(
                txId,
                event.paymentId(),
                event.attemptId(),
                event.provider(),
                result.status(),
                result.code() != null ? result.code().name() : null,
                result.providerPaymentRef()));
        saveOutboxEvent(event, result, txId);
        eventIdempotencyPort.markProcessed(event.eventId());
    }

    private void saveOutboxEvent(PaymentAuthorizationEvent in, AuthorizationResult result, String outId) {
        Instant now = Instant.now();
        switch (result.status()) {
            case AUTHORIZED ->
                    outboxPort.saveSucceeded(new ProviderAuthorizationSucceeded(
                            outId, now, in.paymentId(), in.attemptId(), in.provider(),
                            result.providerPaymentRef(), result.cardDetails()));
            case DECLINED ->
                    outboxPort.saveFailed(new ProviderAuthorizationFailed(
                            outId, now, in.paymentId(), in.attemptId(), in.provider(),
                            result.code(), result.code().description(), result.cardDetails()));
            case REQUIRES_ACTION ->
                    outboxPort.saveActionRequired(new ProviderActionRequired(
                            outId, now, in.paymentId(), in.attemptId(), in.provider(), ""));
        }
    }
}

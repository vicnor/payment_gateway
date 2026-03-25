package com.paymentgateway.payment.application.service;

import com.paymentgateway.payment.api.domain.Payment;
import com.paymentgateway.payment.api.domain.PaymentAttempt;
import com.paymentgateway.payment.api.domain.PaymentAuthorizationRequestedEvent;
import com.paymentgateway.payment.api.domain.WebhookDelivery;
import com.paymentgateway.payment.api.port.in.CreatePaymentUseCase;
import com.paymentgateway.payment.api.port.in.GetPaymentUseCase;
import com.paymentgateway.payment.api.port.in.GetPaymentUseCase.PaymentView;
import com.paymentgateway.payment.api.port.in.HandleAuthorizationResultUseCase;
import com.paymentgateway.payment.api.port.out.CreatePaymentIdempotencyPort;
import com.paymentgateway.payment.api.port.out.EventIdempotencyPort;
import com.paymentgateway.payment.api.port.out.LoadPaymentAttemptPort;
import com.paymentgateway.payment.api.port.out.LoadPaymentPort;
import com.paymentgateway.payment.api.port.out.SaveOutboxEventPort;
import com.paymentgateway.payment.api.port.out.SavePaymentAttemptPort;
import com.paymentgateway.payment.api.port.out.SavePaymentPort;
import com.paymentgateway.payment.api.port.out.SaveWebhookDeliveryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PaymentService implements CreatePaymentUseCase, GetPaymentUseCase, HandleAuthorizationResultUseCase {

    private final SavePaymentPort savePaymentPort;
    private final LoadPaymentPort loadPaymentPort;
    private final SavePaymentAttemptPort savePaymentAttemptPort;
    private final LoadPaymentAttemptPort loadPaymentAttemptPort;
    private final SaveOutboxEventPort saveOutboxEventPort;
    private final EventIdempotencyPort eventIdempotencyPort;
    private final CreatePaymentIdempotencyPort createPaymentIdempotencyPort;
    private final SaveWebhookDeliveryPort saveWebhookDeliveryPort;

    public PaymentService(SavePaymentPort savePaymentPort, LoadPaymentPort loadPaymentPort,
                          SavePaymentAttemptPort savePaymentAttemptPort, LoadPaymentAttemptPort loadPaymentAttemptPort,
                          SaveOutboxEventPort saveOutboxEventPort, EventIdempotencyPort eventIdempotencyPort,
                          CreatePaymentIdempotencyPort createPaymentIdempotencyPort,
                          SaveWebhookDeliveryPort saveWebhookDeliveryPort) {
        this.savePaymentPort = savePaymentPort;
        this.loadPaymentPort = loadPaymentPort;
        this.savePaymentAttemptPort = savePaymentAttemptPort;
        this.loadPaymentAttemptPort = loadPaymentAttemptPort;
        this.saveOutboxEventPort = saveOutboxEventPort;
        this.eventIdempotencyPort = eventIdempotencyPort;
        this.createPaymentIdempotencyPort = createPaymentIdempotencyPort;
        this.saveWebhookDeliveryPort = saveWebhookDeliveryPort;
    }

    @Override
    public Payment createPayment(CreatePaymentCommand command) {
        String idempotencyKey = command.idempotencyKey();

        Optional<Payment> existing = createPaymentIdempotencyPort.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return existing.get();
        }

        Payment payment = Payment.create(
                command.merchantId(),
                command.orderId(),
                command.amount(),
                command.currencyCode(),
                command.captureMode(),
                command.paymentMethodType(),
                command.paymentMethodRef(),
                command.provider(),
                command.callbackUrl()
        );
        Payment savedPayment = savePaymentPort.save(payment);

        PaymentAttempt attempt = PaymentAttempt.create(savedPayment.getId(), command.provider());
        PaymentAttempt savedAttempt = savePaymentAttemptPort.save(attempt);

        PaymentAuthorizationRequestedEvent event = new PaymentAuthorizationRequestedEvent(
                UUID.randomUUID().toString(),
                "PaymentAuthorizationRequested",
                Instant.now(),
                savedPayment.getId(),
                savedAttempt.getAttemptId(),
                savedPayment.getProvider(),
                savedPayment.getAmount(),
                savedPayment.getCurrency().toString(),
                savedPayment.getPaymentMethodType(),
                savedPayment.getPaymentMethodRef()
        );
        saveOutboxEventPort.save(event);

        try {
            createPaymentIdempotencyPort.save(idempotencyKey, savedPayment.getId());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return createPaymentIdempotencyPort.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> new IllegalStateException("Idempotency key conflict but payment not found: " + idempotencyKey));
        }

        return savedPayment;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentView getPayment(UUID paymentId) {
        Payment payment = loadPaymentPort.findById(paymentId).orElseThrow(() -> new PaymentNotFoundException(paymentId));
        PaymentAttempt latestAttempt = loadPaymentAttemptPort.findLatestByPaymentId(paymentId).orElse(null);
        return new PaymentView(payment, latestAttempt);
    }

    @Override
    public void handleSucceeded(AuthorizationSucceededCommand command) {
        if (eventIdempotencyPort.isProcessed(command.eventId())) {
            return;
        }

        Payment payment = loadPaymentPort.findById(command.paymentId()).orElseThrow(() -> new PaymentNotFoundException(command.paymentId()));
        PaymentAttempt attempt = loadPaymentAttemptPort.findByAttemptId(command.attemptId()).orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + command.attemptId()));

        payment.markAuthorized();
        attempt.markAuthorized(command.providerPaymentRef(), command.cardBrand(), command.cardLast4(),
                command.cardExpiryMonth(), command.cardExpiryYear());

        savePaymentPort.save(payment);
        savePaymentAttemptPort.save(attempt);
        eventIdempotencyPort.markProcessed(command.eventId());

        if (payment.getCallbackUrl() != null) {
            String payload = String.format(
                    "{\"eventType\":\"PAYMENT_AUTHORIZED\",\"aggregateType\":\"PAYMENT\",\"aggregateId\":\"%s\",\"merchantId\":\"%s\",\"orderId\":\"%s\",\"amount\":%d,\"currency\":\"%s\",\"status\":\"AUTHORIZED\",\"occurredAt\":\"%s\"}",
                    payment.getId(), payment.getMerchantId(), payment.getOrderId(),
                    payment.getAmount(), payment.getCurrency().getCurrencyCode(), Instant.now()
            );
            saveWebhookDeliveryPort.save(WebhookDelivery.createPending(
                    "PAYMENT", payment.getId(), "PAYMENT_AUTHORIZED",
                    payment.getMerchantId(), payment.getCallbackUrl(), payload
            ));
        }
    }

    @Override
    public void handleFailed(AuthorizationFailedCommand command) {
        if (eventIdempotencyPort.isProcessed(command.eventId())) {
            return;
        }

        Payment payment = loadPaymentPort.findById(command.paymentId()).orElseThrow(() -> new PaymentNotFoundException(command.paymentId()));
        PaymentAttempt attempt = loadPaymentAttemptPort.findByAttemptId(command.attemptId()).orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + command.attemptId()));

        payment.markFailed();
        attempt.markFailed(command.failureCode(), command.failureReason(), command.cardBrand(), command.cardLast4(),
                command.cardExpiryMonth(), command.cardExpiryYear());

        savePaymentPort.save(payment);
        savePaymentAttemptPort.save(attempt);
        eventIdempotencyPort.markProcessed(command.eventId());
    }

    @Override
    public void handleActionRequired(AuthorizationActionRequiredCommand command) {
        if (eventIdempotencyPort.isProcessed(command.eventId())) {
            return;
        }

        Payment payment = loadPaymentPort.findById(command.paymentId()).orElseThrow(() -> new PaymentNotFoundException(command.paymentId()));
        PaymentAttempt attempt = loadPaymentAttemptPort.findByAttemptId(command.attemptId()).orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + command.attemptId()));

        payment.markRequiresCustomerAction();
        attempt.markRequiresAction(command.actionPayload());

        savePaymentPort.save(payment);
        savePaymentAttemptPort.save(attempt);
        eventIdempotencyPort.markProcessed(command.eventId());
    }
}

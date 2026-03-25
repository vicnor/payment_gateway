package com.paymentgateway.payment.infrastructure.rest;

import com.paymentgateway.payment.api.domain.Payment;
import com.paymentgateway.payment.api.port.in.CreatePaymentUseCase;
import com.paymentgateway.payment.api.port.in.CreatePaymentUseCase.CreatePaymentCommand;
import com.paymentgateway.payment.api.port.in.GetPaymentUseCase;
import com.paymentgateway.payment.api.port.in.GetPaymentUseCase.PaymentView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final CreatePaymentUseCase createPaymentUseCase;
    private final GetPaymentUseCase getPaymentUseCase;

    public PaymentController(CreatePaymentUseCase createPaymentUseCase, GetPaymentUseCase getPaymentUseCase) {
        this.createPaymentUseCase = createPaymentUseCase;
        this.getPaymentUseCase = getPaymentUseCase;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest request) {
        try {
            UUID.fromString(idempotencyKey);
        } catch (IllegalArgumentException e) {
            throw new InvalidIdempotencyKeyException(idempotencyKey);
        }
        CreatePaymentCommand command = new CreatePaymentCommand(
                request.merchantId(),
                request.orderId(),
                request.amount(),
                request.currencyCode(),
                request.captureMode(),
                request.paymentMethod().type(),
                request.paymentMethod().token(),
                request.provider(),
                idempotencyKey,
                request.callbackUrl()
        );
        Payment payment = createPaymentUseCase.createPayment(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.from(payment, null));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        PaymentView view = getPaymentUseCase.getPayment(paymentId);
        return ResponseEntity.ok(PaymentResponse.from(view.payment(), view.latestAttempt()));
    }
}

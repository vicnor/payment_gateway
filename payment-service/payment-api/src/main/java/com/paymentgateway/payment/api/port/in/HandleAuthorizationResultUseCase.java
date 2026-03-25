package com.paymentgateway.payment.api.port.in;

import java.util.UUID;

public interface HandleAuthorizationResultUseCase {

    record AuthorizationSucceededCommand(UUID paymentId, UUID attemptId, String eventId,
            String providerPaymentRef, String cardBrand, String cardLast4,
            Integer cardExpiryMonth, Integer cardExpiryYear) {}

    record AuthorizationFailedCommand(UUID paymentId, UUID attemptId, String eventId,
            String failureCode, String failureReason, String cardBrand, String cardLast4,
            Integer cardExpiryMonth, Integer cardExpiryYear) {}

    record AuthorizationActionRequiredCommand(UUID paymentId, UUID attemptId, String eventId, String actionPayload) {}

    void handleSucceeded(AuthorizationSucceededCommand command);

    void handleFailed(AuthorizationFailedCommand command);

    void handleActionRequired(AuthorizationActionRequiredCommand command);
}

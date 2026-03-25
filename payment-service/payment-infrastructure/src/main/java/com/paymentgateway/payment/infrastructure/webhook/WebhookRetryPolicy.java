package com.paymentgateway.payment.infrastructure.webhook;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public final class WebhookRetryPolicy {

    public static final int MAX_ATTEMPTS = 5;

    private static final Duration[] BACKOFF_DELAYS = {
            Duration.ZERO,
            Duration.ofMinutes(1),
            Duration.ofMinutes(5),
            Duration.ofMinutes(30)
    };

    private WebhookRetryPolicy() {
    }

    /**
     * Returns the next retry time after a failure, or empty if max attempts have been reached.
     *
     * @param attemptCountAfterFailure the total number of attempts made so far (including the one that just failed)
     */
    public static Optional<Instant> nextRetryAt(int attemptCountAfterFailure) {
        if (attemptCountAfterFailure >= MAX_ATTEMPTS) {
            return Optional.empty();
        }
        return Optional.of(Instant.now().plus(BACKOFF_DELAYS[attemptCountAfterFailure - 1]));
    }
}

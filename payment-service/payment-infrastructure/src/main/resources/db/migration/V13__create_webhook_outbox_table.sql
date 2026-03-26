CREATE TABLE webhook_outbox (
    id                UUID PRIMARY KEY,
    aggregate_type    VARCHAR(50) NOT NULL,
    aggregate_id      UUID NOT NULL,
    event_type        VARCHAR(100) NOT NULL,
    merchant_id       VARCHAR(255) NOT NULL,
    callback_url      VARCHAR(2048) NOT NULL,
    payload           TEXT NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempt_count     INTEGER NOT NULL DEFAULT 0,
    next_retry_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    last_attempted_at TIMESTAMP WITH TIME ZONE,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_webhook_outbox_pending_retry
    ON webhook_outbox (next_retry_at);

CREATE INDEX idx_webhook_outbox_aggregate
    ON webhook_outbox (aggregate_type, aggregate_id);

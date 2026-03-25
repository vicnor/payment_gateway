CREATE TABLE payment_idempotency_keys (
    idempotency_key VARCHAR(36)              NOT NULL,
    payment_id      UUID                     NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_payment_idempotency_keys PRIMARY KEY (idempotency_key),
    CONSTRAINT fk_payment_idempotency_keys_payment FOREIGN KEY (payment_id) REFERENCES payments(id)
);

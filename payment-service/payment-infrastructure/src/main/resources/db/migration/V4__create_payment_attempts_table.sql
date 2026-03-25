CREATE TABLE payment_attempts (
    attempt_id               UUID         NOT NULL,
    payment_id               UUID         NOT NULL,
    provider                 VARCHAR(50)  NOT NULL,
    status                   VARCHAR(30)  NOT NULL,
    provider_payment_ref     VARCHAR(255),
    failure_code             VARCHAR(100),
    failure_reason           VARCHAR(500),
    requires_action_payload  TEXT,
    created_at               TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at               TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_payment_attempts PRIMARY KEY (attempt_id),
    CONSTRAINT fk_payment_attempts_payment FOREIGN KEY (payment_id) REFERENCES payments(id)
);

CREATE INDEX idx_payment_attempts_payment_id ON payment_attempts (payment_id);
CREATE INDEX idx_payment_attempts_status ON payment_attempts (status);

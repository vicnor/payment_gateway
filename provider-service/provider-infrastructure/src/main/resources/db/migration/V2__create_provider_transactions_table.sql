CREATE TABLE provider_transactions (
    id                   VARCHAR(255)             NOT NULL,
    payment_id           VARCHAR(255)             NOT NULL,
    attempt_id           VARCHAR(255)             NOT NULL,
    provider             VARCHAR(255)             NOT NULL,
    status               VARCHAR(50)              NOT NULL,
    failure_code         VARCHAR(100),
    provider_payment_ref VARCHAR(255),
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_provider_transactions PRIMARY KEY (id)
);

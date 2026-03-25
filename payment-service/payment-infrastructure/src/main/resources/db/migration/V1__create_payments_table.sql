CREATE TABLE payments (
    id          UUID         NOT NULL,
    merchant_id VARCHAR(255) NOT NULL,
    amount      INTEGER        NOT NULL,
    currency    VARCHAR(3)   NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_payments PRIMARY KEY (id)
);

CREATE INDEX idx_payments_merchant_id ON payments (merchant_id);
CREATE INDEX idx_payments_status ON payments (status);

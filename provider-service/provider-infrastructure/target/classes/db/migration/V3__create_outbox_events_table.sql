CREATE TABLE outbox_events (
    id            VARCHAR(255)             NOT NULL,
    event_type    VARCHAR(100)             NOT NULL,
    payment_id    VARCHAR(255)             NOT NULL,
    occurred_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    payload       TEXT                     NOT NULL,
    event_version INTEGER                  NOT NULL DEFAULT 1,
    claimed_at    TIMESTAMP WITH TIME ZONE,
    claimed_by    VARCHAR(255),
    published_at  TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_outbox_events PRIMARY KEY (id)
);
CREATE INDEX idx_outbox_events_published_claimed ON outbox_events (published_at, claimed_at);

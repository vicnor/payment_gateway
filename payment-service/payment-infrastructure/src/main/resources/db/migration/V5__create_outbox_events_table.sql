CREATE TABLE outbox_events (
    event_id      UUID         NOT NULL,
    event_type    VARCHAR(100) NOT NULL,
    occurred_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    payload       TEXT         NOT NULL,
    claimed_at    TIMESTAMP WITH TIME ZONE,
    claimed_by    VARCHAR(255),
    published_at  TIMESTAMP WITH TIME ZONE,

    CONSTRAINT pk_outbox_events PRIMARY KEY (event_id)
);

CREATE INDEX idx_outbox_events_published_at ON outbox_events (published_at);

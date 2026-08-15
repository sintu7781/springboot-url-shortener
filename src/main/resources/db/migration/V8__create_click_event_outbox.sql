CREATE TABLE click_event_outbox (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL,
    short_code VARCHAR(30) NOT NULL,
    clicked_at TIMESTAMPTZ NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(1000),
    referrer VARCHAR(2000),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempt_count INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMPTZ,
    last_error VARCHAR(2000),

    CONSTRAINT uk_click_event_outbox_event_id
        UNIQUE (event_id),

    CONSTRAINT chk_click_event_outbox_status
        CHECK ( status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') )
);

CREATE INDEX idx_click_event_outbox_pending
    ON click_event_outbox (status, next_attempt_at);
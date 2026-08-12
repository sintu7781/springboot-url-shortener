CREATE TABLE click_count_sync_batch (
    batch_key VARCHAR(255) PRIMARY KEY,
    short_code VARCHAR(30) NOT NULL,
    click_count BIGINT NOT NULL,
    processed_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_click_sync_batch_short_code
    ON click_count_sync_batch (short_code);
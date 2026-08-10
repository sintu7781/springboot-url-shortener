CREATE SEQUENCE url_id_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE urls (
    id BIGINT NOT NULL,
    original_url VARCHAR(2048) NOT NULL,
    short_code VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    click_count BIGINT NOT NULL DEFAULT 0,
    expires_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_urls
        PRIMARY KEY (id),

    CONSTRAINT uk_urls_short_code
        UNIQUE (short_code)
);

CREATE UNIQUE INDEX idx_short_code
    ON urls (short_code);
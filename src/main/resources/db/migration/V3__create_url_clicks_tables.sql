CREATE SEQUENCE url_click_id_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE url_clicks (
    id BIGINT NOT NULL DEFAULT nextval('url_click_id_seq'),
    url_id BIGINT NOT NULL,
    clicked_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(1000),
    referrer VARCHAR(2000),

    CONSTRAINT pk_url_clicks
        PRIMARY KEY (id),

    CONSTRAINT fk_url_clicks_url
        FOREIGN KEY (url_id)
        REFERENCES urls (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_url_click_url_id
    ON url_clicks (url_id);

CREATE INDEX idx_url_click_clicked_at
    ON url_clicks (clicked_at);
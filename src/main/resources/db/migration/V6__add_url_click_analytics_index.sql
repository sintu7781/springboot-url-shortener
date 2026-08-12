CREATE INDEX idx_url_click_url_id_clicked_at
    ON url_clicks (url_id, clicked_at);
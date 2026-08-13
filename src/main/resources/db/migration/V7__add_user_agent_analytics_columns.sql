ALTER TABLE url_clicks
    ADD COLUMN browser VARCHAR(100),
    ADD COLUMN operating_system VARCHAR(100),
    ADD COLUMN device_type VARCHAR(50);
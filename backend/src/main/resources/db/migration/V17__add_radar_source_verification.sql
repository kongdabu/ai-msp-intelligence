ALTER TABLE radar_signal
    ADD COLUMN source_verified_at TIMESTAMP;

CREATE INDEX idx_radar_signal_status_captured_at
    ON radar_signal(status, captured_at DESC);

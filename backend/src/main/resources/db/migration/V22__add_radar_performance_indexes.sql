-- Radar 성능 최적화 복합 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_radar_signal_lens_lookup ON radar_signal_lens(lens, signal_id);
CREATE INDEX IF NOT EXISTS idx_radar_signal_sort ON radar_signal(status, impact_score DESC, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_radar_signal_time ON radar_signal(occurred_at DESC, captured_at DESC);
CREATE INDEX IF NOT EXISTS idx_radar_assessment_signal_id ON radar_assessment(signal_id);

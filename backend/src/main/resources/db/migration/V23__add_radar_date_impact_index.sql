-- Radar 신호 일자별-영향도 정렬 조회 성능 최적화 복합 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_radar_signal_date_impact_sort ON radar_signal(status, occurred_at DESC, impact_score DESC);

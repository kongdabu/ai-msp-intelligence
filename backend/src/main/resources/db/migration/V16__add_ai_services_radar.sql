CREATE TABLE radar_player (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    layer VARCHAR(40) NOT NULL,
    country VARCHAR(40) NOT NULL,
    website VARCHAR(500),
    watch_priority INTEGER NOT NULL DEFAULT 3,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE radar_signal (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    fact TEXT NOT NULL,
    source_url VARCHAR(2000) NOT NULL UNIQUE,
    source_tier VARCHAR(20) NOT NULL,
    signal_type VARCHAR(40) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    captured_at TIMESTAMP NOT NULL,
    confidence_score INTEGER NOT NULL,
    impact_score INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW'
);

CREATE TABLE radar_signal_player (
    signal_id BIGINT NOT NULL REFERENCES radar_signal(id) ON DELETE CASCADE,
    player_id BIGINT NOT NULL REFERENCES radar_player(id) ON DELETE CASCADE,
    PRIMARY KEY (signal_id, player_id)
);

CREATE TABLE radar_signal_lens (
    signal_id BIGINT NOT NULL REFERENCES radar_signal(id) ON DELETE CASCADE,
    lens VARCHAR(40) NOT NULL,
    PRIMARY KEY (signal_id, lens)
);

CREATE TABLE radar_assessment (
    id BIGSERIAL PRIMARY KEY,
    signal_id BIGINT NOT NULL UNIQUE REFERENCES radar_signal(id) ON DELETE CASCADE,
    what_changed TEXT NOT NULL,
    industry_structure_impact TEXT NOT NULL,
    msp_opportunity TEXT,
    msp_threat TEXT,
    structural_risk TEXT,
    recommended_action TEXT NOT NULL,
    delivery_model VARCHAR(80),
    pricing_model VARCHAR(80),
    generated_at TIMESTAMP NOT NULL
);

CREATE TABLE radar_weekly_brief (
    id BIGSERIAL PRIMARY KEY,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    title VARCHAR(300) NOT NULL,
    executive_summary TEXT NOT NULL,
    player_moves TEXT NOT NULL,
    partnership_changes TEXT NOT NULL,
    delivery_model_changes TEXT NOT NULL,
    pricing_changes TEXT NOT NULL,
    agentic_operations_changes TEXT NOT NULL,
    korea_impact TEXT NOT NULL,
    generated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_radar_player_layer ON radar_player(layer);
CREATE INDEX idx_radar_signal_occurred_at ON radar_signal(occurred_at);
CREATE INDEX idx_radar_signal_status ON radar_signal(status);
CREATE INDEX idx_radar_weekly_brief_period ON radar_weekly_brief(period_start, period_end);

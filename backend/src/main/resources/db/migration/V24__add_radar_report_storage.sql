-- 외부 AI Services Radar 산출물 저장용 보고서·신호·출처 구조
CREATE TABLE radar_report (
    id BIGSERIAL PRIMARY KEY,
    report_date DATE NOT NULL,
    report_type VARCHAR(80) NOT NULL,
    title VARCHAR(300) NOT NULL,
    executive_view TEXT NOT NULL,
    strategic_interpretation TEXT NOT NULL,
    markdown TEXT NOT NULL,
    prompt_version VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_radar_report_type_date UNIQUE (report_type, report_date)
);

CREATE TABLE radar_report_signal (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL REFERENCES radar_report(id) ON DELETE CASCADE,
    company VARCHAR(160) NOT NULL,
    category VARCHAR(80) NOT NULL,
    importance VARCHAR(40) NOT NULL,
    signal TEXT NOT NULL,
    fact TEXT NOT NULL,
    what_changed TEXT NOT NULL,
    industry_impact TEXT NOT NULL,
    opportunity TEXT,
    threat TEXT,
    structural_risk TEXT,
    practical_implication TEXT,
    recommended_action TEXT NOT NULL
);

CREATE TABLE radar_report_source (
    id BIGSERIAL PRIMARY KEY,
    signal_id BIGINT NOT NULL REFERENCES radar_report_signal(id) ON DELETE CASCADE,
    publisher VARCHAR(200) NOT NULL,
    title VARCHAR(500) NOT NULL,
    url VARCHAR(2000) NOT NULL,
    published_date DATE,
    source_type VARCHAR(80) NOT NULL
);

CREATE INDEX idx_radar_report_date_type ON radar_report(report_date DESC, report_type);
CREATE INDEX idx_radar_report_signal_filters ON radar_report_signal(company, category, importance);
CREATE INDEX idx_radar_report_source_signal ON radar_report_source(signal_id);

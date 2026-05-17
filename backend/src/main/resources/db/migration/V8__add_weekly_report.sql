CREATE TABLE weekly_report (
    id                       BIGSERIAL PRIMARY KEY,
    title                    VARCHAR(200),
    competitor_trends        TEXT,
    ai_trends                TEXT,
    strategy_recommendations TEXT,
    week_start               DATE,
    week_end                 DATE,
    article_count            INTEGER,
    insight_count            INTEGER,
    docx_path                VARCHAR(500),
    generated_at             TIMESTAMP
);

CREATE INDEX idx_weekly_report_week_start ON weekly_report (week_start);

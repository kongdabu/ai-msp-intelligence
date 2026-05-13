CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT PRIMARY KEY,
    max_articles_for_insight INTEGER NOT NULL DEFAULT 50,
    max_insights_per_generation INTEGER NOT NULL DEFAULT 8
);

INSERT INTO system_config (id, max_articles_for_insight, max_insights_per_generation)
VALUES (1, 50, 8)
ON CONFLICT (id) DO NOTHING;

ALTER TABLE system_config ADD COLUMN min_relevance_score_for_insight INTEGER DEFAULT 65;
UPDATE system_config SET min_relevance_score_for_insight = 65 WHERE id = 1;

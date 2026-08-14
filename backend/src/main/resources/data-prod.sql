INSERT INTO system_config (id, max_articles_for_insight, max_insights_per_generation, min_relevance_score_for_insight)
VALUES (1, 150, 8, 65)
ON CONFLICT (id) DO UPDATE SET max_articles_for_insight = 150;

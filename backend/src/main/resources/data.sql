MERGE INTO system_config (id, max_articles_for_insight, max_insights_per_generation, min_relevance_score_for_insight)
KEY(id) VALUES (1, 50, 8, 65);

ALTER TABLE article ADD COLUMN IF NOT EXISTS analysis_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

UPDATE article
SET analysis_status = CASE
    WHEN summary IS NOT NULL AND relevance_score IS NOT NULL THEN 'COMPLETED'
    ELSE 'PENDING'
END
WHERE analysis_status IS NULL OR analysis_status = 'PENDING';

CREATE INDEX IF NOT EXISTS idx_article_analysis_status_collected_at
    ON article(analysis_status, collected_at);

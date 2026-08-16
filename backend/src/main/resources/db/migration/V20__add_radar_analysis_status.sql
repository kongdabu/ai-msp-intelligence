ALTER TABLE article ADD COLUMN IF NOT EXISTS radar_analysis_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
ALTER TABLE article ADD COLUMN IF NOT EXISTS radar_analyzed_at TIMESTAMP;

UPDATE article
SET radar_analysis_status = CASE
    WHEN url IN (SELECT source_url FROM radar_signal) THEN 'COMPLETED'
    ELSE 'PENDING'
END
WHERE radar_analysis_status IS NULL OR radar_analysis_status = 'PENDING';

CREATE INDEX IF NOT EXISTS idx_article_radar_analysis_queue
    ON article(analysis_status, radar_analysis_status, collected_at);

-- gemini-embedding-2 모델 기준 3072 차원으로 변경
-- (text-embedding-004 미지원으로 모델 교체, 기존 768→3072)
ALTER TABLE article DROP COLUMN IF EXISTS embedding;
ALTER TABLE article ADD COLUMN IF NOT EXISTS embedding vector(3072);

DROP INDEX IF EXISTS idx_article_embedding;
CREATE INDEX IF NOT EXISTS idx_article_embedding
    ON article USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

CREATE EXTENSION IF NOT EXISTS vector;

ALTER TABLE article ADD COLUMN IF NOT EXISTS embedding vector(768);

CREATE INDEX IF NOT EXISTS idx_article_embedding
    ON article USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

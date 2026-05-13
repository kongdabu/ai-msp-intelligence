CREATE EXTENSION IF NOT EXISTS vector;

ALTER TABLE article ADD COLUMN IF NOT EXISTS embedding vector(768);

-- CONCURRENTLY는 트랜잭션 내 실행 불가 (Flyway 제약) — 일반 인덱스로 생성
CREATE INDEX IF NOT EXISTS idx_article_embedding
    ON article USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

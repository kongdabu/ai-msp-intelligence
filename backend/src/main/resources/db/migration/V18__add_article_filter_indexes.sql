-- 기사 목록의 관심 주제·출처·발행일 필터를 위한 운영 PostgreSQL 인덱스
CREATE INDEX IF NOT EXISTS idx_article_category_published_at
    ON article(category, published_at DESC);

CREATE INDEX IF NOT EXISTS idx_article_source_type_published_at
    ON article(source_type, published_at DESC);

CREATE INDEX IF NOT EXISTS idx_article_bookmarked_at
    ON article(bookmarked, bookmarked_at DESC);

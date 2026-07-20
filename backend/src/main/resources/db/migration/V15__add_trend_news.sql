CREATE TABLE trend_news (
    id BIGSERIAL PRIMARY KEY,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    title VARCHAR(200) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    trend_score INTEGER,
    confidence_score INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    generated_at TIMESTAMP NOT NULL
);

CREATE TABLE trend_news_keywords (
    trend_news_id BIGINT NOT NULL REFERENCES trend_news(id) ON DELETE CASCADE,
    keyword VARCHAR(100)
);

CREATE TABLE trend_news_action_items (
    trend_news_id BIGINT NOT NULL REFERENCES trend_news(id) ON DELETE CASCADE,
    action_item VARCHAR(500)
);

CREATE TABLE trend_news_articles (
    id BIGSERIAL PRIMARY KEY,
    trend_news_id BIGINT NOT NULL REFERENCES trend_news(id) ON DELETE CASCADE,
    article_id BIGINT NOT NULL REFERENCES article(id),
    relevance_score INTEGER,
    CONSTRAINT uk_trend_news_article UNIQUE (trend_news_id, article_id)
);

CREATE INDEX idx_trend_news_generated_at ON trend_news(generated_at);
CREATE INDEX idx_trend_news_period ON trend_news(period_start, period_end);
CREATE INDEX idx_trend_news_status ON trend_news(status);
CREATE INDEX idx_trend_news_articles_news ON trend_news_articles(trend_news_id);

-- insight_articles 테이블을 @ManyToMany 구조에서 @OneToMany 엔티티 구조로 전환
-- relevance_score(인사이트-기사 관련도) 컬럼 추가

CREATE TABLE insight_articles_new (
    id         BIGSERIAL PRIMARY KEY,
    insight_id BIGINT  NOT NULL REFERENCES insight(id),
    article_id BIGINT  NOT NULL REFERENCES article(id),
    relevance_score INTEGER
);

INSERT INTO insight_articles_new (insight_id, article_id)
SELECT insight_id, article_id FROM insight_articles;

DROP TABLE insight_articles;
ALTER TABLE insight_articles_new RENAME TO insight_articles;

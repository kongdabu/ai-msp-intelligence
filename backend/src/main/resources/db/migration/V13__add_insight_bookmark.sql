-- 인사이트 북마크(저장) 기능: 나중에 다시 조회/리마인드용
ALTER TABLE insight ADD COLUMN bookmarked BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE insight ADD COLUMN bookmarked_at TIMESTAMP;
ALTER TABLE insight ADD COLUMN bookmark_note VARCHAR(500);

CREATE INDEX idx_insight_bookmarked ON insight (bookmarked);

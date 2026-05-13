CREATE TABLE qa_session (
    id         BIGSERIAL PRIMARY KEY,
    title      VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE qa_message (
    id                 BIGSERIAL PRIMARY KEY,
    session_id         BIGINT NOT NULL REFERENCES qa_session(id) ON DELETE CASCADE,
    role               VARCHAR(10) NOT NULL,
    content            TEXT NOT NULL,
    source_article_ids TEXT,
    created_at         TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_qa_message_session_id ON qa_message(session_id);
CREATE INDEX idx_qa_session_created_at ON qa_session(created_at DESC);

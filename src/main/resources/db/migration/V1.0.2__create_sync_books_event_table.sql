CREATE TABLE sync_books_events
(
    id            BIGSERIAL PRIMARY KEY,
    book_id       VARCHAR(36) NOT NULL,
    processed     BOOLEAN     NOT NULL DEFAULT FALSE,
    status        VARCHAR(64) NOT NULL,
    error_message VARCHAR(456),
    created_at    BIGINT      NOT NULL,
    updated_at    BIGINT
);

CREATE INDEX idx_sync_books_event_timestamp ON sync_books_events (created_at);
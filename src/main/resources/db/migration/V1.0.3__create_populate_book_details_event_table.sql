CREATE TABLE populate_book_details_events
(
    id            BIGSERIAL PRIMARY KEY,
    book_id       VARCHAR(36) NOT NULL,
    status        VARCHAR(64) NOT NULL,
    error_message VARCHAR(456),
    created_at    BIGINT      NOT NULL,
    updated_at    BIGINT
);

CREATE INDEX idx_populate_book_details_event_timestamp ON populate_book_details_events (created_at);
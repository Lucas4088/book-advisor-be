CREATE TABLE book_details_fetched_events
(
    id            BIGSERIAL PRIMARY KEY,

    source_name   VARCHAR(255) NOT NULL,

    status        VARCHAR(50)  NOT NULL,
    error_message TEXT,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP
);

CREATE INDEX idx_book_details_fetched_events_create_at
    ON book_details_fetched_events(created_at);
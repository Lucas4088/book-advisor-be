CREATE TABLE scheduled_book_crawler_on_demand_events
(
    id            BIGSERIAL PRIMARY KEY,

    book_id       VARCHAR(255) NOT NULL,

    crawler_id    BIGINT       NOT NULL,

    status        VARCHAR(50)  NOT NULL,
    error_message TEXT,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP,
    retry_count   INTEGER      NOT NULL DEFAULT 0,
    last_retry_at TIMESTAMP,
    next_retry_at TIMESTAMP
);

CREATE INDEX idx_book_id_crawler_id ON scheduled_book_crawler_on_demand_events (book_id, crawler_id);
CREATE INDEX idx_book_id_crawler_id_status ON scheduled_book_crawler_on_demand_events (book_id, crawler_id, status);


CREATE INDEX idx_crawler_id_status ON scheduled_book_crawler_events (book_id, crawler_id);

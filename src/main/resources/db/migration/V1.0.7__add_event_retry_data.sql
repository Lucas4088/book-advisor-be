ALTER TABLE scheduled_book_crawler_events
    ADD COLUMN retry_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN last_retry_at TIMESTAMP,
    ADD COLUMN next_retry_at TIMESTAMP;
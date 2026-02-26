CREATE TABLE scheduled_book_crawler_events (
                            id BIGSERIAL PRIMARY KEY,

                            book_id VARCHAR(255) NOT NULL,

                            crawler_id BIGINT NOT NULL,

                            status VARCHAR(50) NOT NULL,
                            error_message TEXT,
                            created_at TIMESTAMP NOT NULL,
                            updated_at TIMESTAMP NOT NULL
);

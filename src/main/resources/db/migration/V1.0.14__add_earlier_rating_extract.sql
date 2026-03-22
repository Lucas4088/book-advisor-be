ALTER TABLE crawler_configs
    ADD COLUMN is_rating_available_on_search BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN book_rating_count_search_selector VARCHAR(255),
    ADD COLUMN book_rating_score_search_selector VARCHAR(255);
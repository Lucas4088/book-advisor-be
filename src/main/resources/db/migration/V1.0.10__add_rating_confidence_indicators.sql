ALTER TABLE ratings
    ADD COLUMN
        title_confidence_indicator NUMERIC(3, 2) NOT NULL DEFAULT 1.0;

ALTER TABLE ratings
    ADD COLUMN
        authors_confidence_indicator NUMERIC(3, 2) NOT NULL DEFAULT 1.0;

ALTER TABLE crawler_configs
    ADD COLUMN
        book_title_selector TEXT NOT NULL DEFAULT '';

ALTER TABLE crawler_configs
    ADD COLUMN
        book_authors_selector TEXT NOT NULL DEFAULT '';
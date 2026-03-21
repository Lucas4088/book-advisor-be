ALTER TABLE crawler_configs
    ADD COLUMN include_authors_for_search BOOLEAN NOT NULL DEFAULT false;
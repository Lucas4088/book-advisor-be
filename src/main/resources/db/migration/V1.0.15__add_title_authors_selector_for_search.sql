ALTER TABLE crawler_configs
    ADD COLUMN book_first_element_search_selector VARCHAR(255),
    ADD COLUMN book_title_search_selector VARCHAR(255),
    ADD COLUMN book_authors_search_selector VARCHAR(255);
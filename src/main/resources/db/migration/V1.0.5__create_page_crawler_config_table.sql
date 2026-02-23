CREATE TABLE crawler_configs
(
    id                         BIGSERIAL PRIMARY KEY,
    enabled                    BOOLEAN      NOT NULL,
    base_url                   VARCHAR(500) NOT NULL,
    proxy_enabled              BOOLEAN      NOT NULL,

    requests_per_minute        INTEGER      NOT NULL,
    burst                      INTEGER      NOT NULL,

    book_result_selector       TEXT         NOT NULL,
    book_rating_count_selector TEXT         NOT NULL,
    book_rating_score_selector TEXT         NOT NULL,
    search                     TEXT         NOT NULL,
    title_space_separator      VARCHAR(10)  NOT NULL
);

ALTER TABLE crawler_configs
    ADD CONSTRAINT uq_crawler_base_url UNIQUE (base_url);
CREATE TABLE book_basic_data_population_events
(
    id            BIGSERIAL PRIMARY KEY,
    year          INTEGER      NOT NULL,
    lang          VARCHAR(255) NOT NULL,
    status        VARCHAR(64)  NOT NULL,
    error_message VARCHAR(456),
    created_at    BIGINT       NOT NULL,
    updated_at     BIGINT
);
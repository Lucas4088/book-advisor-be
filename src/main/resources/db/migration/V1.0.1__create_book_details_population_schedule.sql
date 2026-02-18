CREATE TABLE book_basic_data_population_scheduled_years
(
    id        BIGSERIAL PRIMARY KEY,
    year      INTEGER      NOT NULL,
    lang      VARCHAR(255) NOT NULL,
    processed BOOLEAN      NOT NULL DEFAULT FALSE,
    timestamp BIGINT       NOT NULL
);
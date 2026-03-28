CREATE TABLE book_editions
(
    id       BIGINT PRIMARY KEY,
    book_id  VARCHAR(36)  NOT NULL,
    title    VARCHAR(255) NOT NULL,
    language VARCHAR(255) NOT NULL,

    CONSTRAINT fk_books
        FOREIGN KEY (book_id)
            REFERENCES books (id)
);

CREATE INDEX idx_book_editions_lang ON book_editions (language);
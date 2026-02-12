CREATE TABLE books (
                       id VARCHAR(36) PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       description TEXT,
                       publishing_year INT NOT NULL,
                       page_count INT NOT NULL,
                       thumbnail_url TEXT,
                       small_thumbnail_url TEXT
);
CREATE INDEX idx_books_publishing_year ON books(publishing_year);

CREATE TABLE authors (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(255) NOT NULL
);

CREATE TABLE genres (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(255) NOT NULL
);

CREATE TABLE book_authors (
                              book_id VARCHAR(36) NOT NULL,
                              author_id BIGINT NOT NULL,

                              PRIMARY KEY (book_id, author_id),

                              CONSTRAINT fk_book_authors_book
                                  FOREIGN KEY (book_id)
                                      REFERENCES books(id)
                                      ON DELETE CASCADE,

                              CONSTRAINT fk_book_authors_author
                                  FOREIGN KEY (author_id)
                                      REFERENCES authors(id)
                                      ON DELETE CASCADE
);
CREATE INDEX idx_book_authors_author_id ON book_authors(author_id);

CREATE TABLE book_genres (
                             book_id VARCHAR(36) NOT NULL,
                             genre_id BIGINT NOT NULL,

                             PRIMARY KEY (book_id, genre_id),

                             CONSTRAINT fk_book_genres_book
                                 FOREIGN KEY (book_id)
                                     REFERENCES books(id)
                                     ON DELETE CASCADE,

                             CONSTRAINT fk_book_genres_genre
                                 FOREIGN KEY (genre_id)
                                     REFERENCES genres(id)
                                     ON DELETE CASCADE
);
CREATE INDEX idx_book_genres_genre_id ON book_genres(genre_id);

CREATE TABLE rating_sources (
                                id SERIAL PRIMARY KEY,
                                name VARCHAR(255) NOT NULL,
                                url TEXT NOT NULL
);

CREATE TABLE ratings (
                         id BIGSERIAL PRIMARY KEY,
                         book_id VARCHAR(36) NOT NULL,
                         source_id INT NOT NULL,

                         rating NUMERIC(3,2) NOT NULL,
                         count INT,

                         CONSTRAINT fk_ratings_book
                             FOREIGN KEY (book_id)
                                 REFERENCES books(id)
                                 ON DELETE CASCADE,

                         CONSTRAINT fk_ratings_source
                             FOREIGN KEY (source_id)
                                 REFERENCES rating_sources(id)
);
CREATE INDEX idx_ratings_book_id ON ratings(book_id);




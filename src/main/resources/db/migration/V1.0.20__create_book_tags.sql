CREATE TABLE tags (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(255) NOT NULL
);

CREATE TABLE book_tags (
                             book_id VARCHAR(36) NOT NULL,
                             tag_id BIGINT NOT NULL,

                             PRIMARY KEY (book_id, tag_id),

                             CONSTRAINT fk_book_tags_book
                                 FOREIGN KEY (book_id)
                                     REFERENCES books(id)
                                     ON DELETE CASCADE,

                             CONSTRAINT fk_book_tags_tag
                                 FOREIGN KEY (tag_id)
                                     REFERENCES tags(id)
                                     ON DELETE CASCADE
);
CREATE INDEX idx_book_tags_tag_id ON book_tags(tag_id);
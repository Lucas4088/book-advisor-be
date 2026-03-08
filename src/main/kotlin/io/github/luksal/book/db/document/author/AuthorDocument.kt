package io.github.luksal.book.db.document.author

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "authors")
data class AuthorDocument(
    @Id
    val id: String,

    val name: String,
)
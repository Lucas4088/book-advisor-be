package io.github.luksal.book.db.document.model

import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "book-basic-info-documents")
class BookBasicInfoDocument(
    val id: String,
    val title: String,
    val key: String,
    val editionKey: String?,
    val editionTitle: String?,
    val lang: String
)
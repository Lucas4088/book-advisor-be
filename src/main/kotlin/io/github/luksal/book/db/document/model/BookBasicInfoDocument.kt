package io.github.luksal.book.db.document.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "book-basic-info-documents")
class BookBasicInfoDocument(
    @Id
    val id: String,
    @Indexed(unique = true)
    val publicId: String,
    val title: String,
    val key: String,
    val editionKey: String?,
    val editionTitle: String?,
    val firstPublishYear: Int?,
    val authors: List<String>,
    val lang: String
)
package io.github.luksal.book.db.document.bookbasicinfo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "book-basic-info")
class BookBasicInfoDocument(
    @Id
    val id: String,
    @Indexed(unique = true)
    val publicId: String,
    val title: String,
    val openLibraryKey: String?,
    val openLibraryEditionKey: String?,
    val editionTitle: String?,
    val firstPublishYear: Int?,
    val authors: List<String> = emptyList(),
    val lang: String
)
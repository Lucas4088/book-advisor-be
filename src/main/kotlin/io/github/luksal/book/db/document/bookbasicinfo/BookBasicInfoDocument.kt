package io.github.luksal.book.db.document.bookbasicinfo

import io.github.luksal.util.ext.normalizeWhiteChars
import io.github.luksal.util.ext.sha256
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "book-basic-info")
class BookBasicInfoDocument(
    @Indexed(unique = true)
    val publicId: String,
    val title: String,
    val openLibraryKey: String? = null,
    val openLibraryEditionKey: String? = null,
    val editionTitle: String = title,
    @Id
    val id: String = generateId(title, editionTitle),
    val firstPublishYear: Int? = null,
    val firstPublishDate: String? = null,
    val authors: List<String> = emptyList(),
    val authorsKeys: List<String> = emptyList(),
    val lang: String,
    val subjects: List<String> = emptyList(),
    val description: String?,
) {
    companion object {
        private fun generateId(title: String, editionTitle: String): String =
            ("$title|$editionTitle").normalizeWhiteChars().sha256()
    }
}
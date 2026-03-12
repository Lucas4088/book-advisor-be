package io.github.luksal.book.db.document.author

import io.github.luksal.util.ext.murmurHash3
import io.github.luksal.util.ext.normalizeWhiteChars
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "authors")
data class AuthorDocument(
    @Id
    val key: String,
    @Indexed(unique = true)
    val publicId: String,
    val name: String,
) {
    companion object {
        fun generatePublicId(key: String, name: String): String =
            ("$key|$name").normalizeWhiteChars().murmurHash3()
    }
}
package io.github.luksal.book.db.document.book

import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.model.Book
import io.github.luksal.book.model.BookEdition
import io.github.luksal.book.model.Rating
import io.github.luksal.book.model.RatingSource
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal

@Document(collection = "books")
class BookDocument(
    @Id
    val id: String,
    val title: String,
    val description: String?,
    val publishingYear: Int?,
    val pageCount: Int,

    val edition: EditionEmbedded? = null,

    val thumbnailUrl: String,
    val smallThumbnailUrl: String,

    val authors: List<AuthorEmbedded>? = emptyList(),
    val genres: List<GenreEmbedded>? = emptyList(),
    val ratings: Set<RatingEmbedded>? = emptySet()
)

data class EditionEmbedded(
    val title: String,
    val lang: String
)

data class AuthorEmbedded(
    val publicId: String?,
    val name: String
)

data class GenreEmbedded(
    val name: String
)

data class RatingEmbedded(
    val rating: BigDecimal,
    val count: Int,
    val source: RatingSourceEmbedded
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RatingEmbedded) return false
        return source.name == other.source.name
    }

    override fun hashCode(): Int = source.name.hashCode()
}

data class RatingSourceEmbedded(
    val name: String,
    val url: String
)
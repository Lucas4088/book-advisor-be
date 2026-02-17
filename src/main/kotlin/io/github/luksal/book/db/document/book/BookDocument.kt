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
    val description: String,
    val publishingYear: Int?,
    val pageCount: Int,

    val edition: EditionEmbedded? = null,

    val thumbnailUrl: String,
    val smallThumbnailUrl: String,

    val authors: List<AuthorEmbedded>? = emptyList(),
    val genres: List<GenreEmbedded>? = emptyList(),
    val ratings: Set<RatingEmbedded>? = emptySet()
) {
    companion object {
        fun fromModel(book: Book): BookDocument = BookDocument(
            id = book.id,
            title = book.title,
            description = book.description,
            publishingYear = book.publishingYear?.value,
            pageCount = book.pageCount,
            edition = book.edition?.let { EditionEmbedded.fromModel(it) },
            thumbnailUrl = book.thumbnailUrl,
            smallThumbnailUrl = book.smallThumbnailUrl,
            authors = book.authors.map { AuthorEmbedded(it.publicId, it.name) },
            genres = book.genres.map { GenreEmbedded(it.id, it.name) },
            ratings = book.ratings.map { rating ->
                RatingEmbedded.fromModel(rating)
            }.toSet()
        )

        fun toSearchResponse(book: BookDocument): BookSearchResponse = BookSearchResponse(
            id = book.id,
            title = book.title,
            smallThumbnailUrl = book.smallThumbnailUrl
        )
    }
}

data class EditionEmbedded(
    val title: String,
    val lang: String
) {
    companion object {
        fun fromModel(bookEdition: BookEdition): EditionEmbedded = EditionEmbedded(
            title = bookEdition.title,
            lang = bookEdition.lang
        )
    }
}

data class AuthorEmbedded(
    val publicId: String?,
    val name: String
)

data class GenreEmbedded(
    val id: Long,
    val name: String
)

data class RatingEmbedded(
    val rating: BigDecimal,
    val count: Int,
    val source: RatingSourceEmbedded
) {
    companion object {
        fun fromModel(rating: Rating): RatingEmbedded = RatingEmbedded(
            rating = rating.score,
            count = rating.count,
            source = RatingSourceEmbedded.fromModel(rating.source)
        )
    }

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
) {
    companion object {
        fun fromModel(source: RatingSource): RatingSourceEmbedded = RatingSourceEmbedded(
            name = source.name,
            url = source.url
        )
    }
}
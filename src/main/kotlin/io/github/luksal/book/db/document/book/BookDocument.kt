package io.github.luksal.book.db.document.book

import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.model.Book
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

    val thumbnailUrl: String,
    val smallThumbnailUrl: String,

    val authors: List<AuthorEmbedded>,
    val genres: List<GenreEmbedded>,
    val ratings: List<RatingEmbedded>
) {
    companion object {
        fun fromModel(book: Book): BookDocument = BookDocument(
            id = book.id,
            title = book.title,
            description = book.description,
            publishingYear = book.publishingYear?.value,
            pageCount = book.pageCount,
            thumbnailUrl = book.thumbnailUrl,
            smallThumbnailUrl = book.smallThumbnailUrl,
            authors = book.authors.map { AuthorEmbedded(it.id, it.name) },
            genres = book.genres.map { GenreEmbedded(it.id, it.name) },
            ratings = book.ratings.map { rating ->
                RatingEmbedded(
                    id = rating.id,
                    rating = rating.rating,
                    count = rating.count,
                    source = RatingSourceEmbedded(
                        id = rating.source.id,
                        name = rating.source.name,
                        url = rating.source.url
                    )
                )
            }
        )

        fun toSearchResponse(book: BookDocument): BookSearchResponse = BookSearchResponse(
            id = book.id,
            title = book.title,
            smallThumbnailUrl = book.smallThumbnailUrl
        )
    }

}

data class AuthorEmbedded(
    val id: Long,
    val name: String
)

data class GenreEmbedded(
    val id: Long,
    val name: String
)

data class RatingEmbedded(
    val id: Long,
    val rating: BigDecimal,
    val count: Int,
    val source: RatingSourceEmbedded
)

data class RatingSourceEmbedded(
    val id: Int,
    val name: String,
    val url: String
)
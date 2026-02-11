package io.github.luksal.book.model

import io.github.luksal.book.db.document.model.AuthorEmbedded
import io.github.luksal.book.db.document.model.BookDocument
import io.github.luksal.book.db.document.model.GenreEmbedded
import io.github.luksal.book.db.document.model.RatingEmbedded
import io.github.luksal.book.db.document.model.RatingSourceEmbedded
import org.springframework.data.mongodb.core.index.Indexed
import java.math.BigDecimal
import java.time.Year

data class Book(
    val publicId: String,
    val title: String,
    val description: String,
    val publishingYear: Year?,
    val pageCount: Int,
    val thumbnailUrl: String,
    val smallThumbnailUrl: String,
    val authors: List<Author>,
    val genres: List<Genre>,
    val ratings: List<Rating>,
)

data class Author(
    val id: Long,
    val name: String
)

data class Genre(
    val id: Long,
    val name: String
)

data class Rating(
    val id: Long,
    val rating: BigDecimal,
    val count: Int,
    val source: RatingSource
)

data class RatingSource(
    val id: Int,
    val name: String,
    val url: String
)

fun Book.toDocument(): BookDocument = BookDocument(
    id = publicId,
    title = title,
    description = description,
    publishingYear = publishingYear?.value,
    pageCount = pageCount,
    thumbnailUrl = thumbnailUrl,
    smallThumbnailUrl = smallThumbnailUrl,
    authors = authors.map { AuthorEmbedded(it.id, it.name) },
    genres = genres.map { GenreEmbedded(it.id, it.name) },
    ratings = ratings.map { rating ->
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
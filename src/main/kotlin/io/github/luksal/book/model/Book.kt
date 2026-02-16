package io.github.luksal.book.model

import java.math.BigDecimal
import java.time.Year

data class Book(
    val id: String,
    val title: String,
    val description: String,
    val publishingYear: Year?,
    val pageCount: Int,
    val edition: BookEdition? = null,
    val thumbnailUrl: String,
    val smallThumbnailUrl: String,
    val authors: List<Author>,
    val genres: List<Genre>,
    val ratings: List<Rating>,
)

data class BookEdition(
    val title: String,
    val lang: String
)

data class Author(
    val id: Long,
    val publicId: String,
    val name: String
)

data class Genre(
    val id: Long,
    val name: String
)

data class Rating(
    val id: Long? = null,
    val score: BigDecimal,
    val count: Int,
    val source: RatingSource
)

data class RatingSource(
    val id: Int? = null,
    val name: String,
    val url: String
)
package io.github.luksal.book.model

import java.math.BigDecimal
import java.time.Year

data class Book(
    val id: Long,
    val title: String,
    val description: String,
    val publishingYear: Year,
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
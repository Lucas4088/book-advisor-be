package io.github.luksal.book.model

import java.math.BigDecimal
import java.time.Year

data class BookUpdate(
    val id: String,
    val description: String? = null,
    val publishingYear: Year? = null,
    val pageCount: Int? = null,
    val edition: BookEdition? = null,
    val thumbnailUrl: String? = null,
    val smallThumbnailUrl: String? = null,
    val authors: List<AuthorUpdate>? = null,
    val genres: List<GenreUpdate>? = null,
    val ratings: List<RatingUpdate>? = null,
)

data class BookEditionUpdate(
    val title: String?,
    val lang: String?
)

data class AuthorUpdate(
    val id: Long?,
    val publicId: String?,
    val name: String,
    val otherNames: List<String>? = null
)

data class GenreUpdate(
    val id: Long?,
    val name: String
)

data class RatingUpdate(
    val id: Long? = null,
    val score: BigDecimal,
    val count: Int,
    val source: RatingSourceUpdate
)

data class RatingSourceUpdate(
    val id: Int? = null,
    val name: String,
    val url: String
)
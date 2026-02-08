package io.github.luksal.book.db.document;

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal

@Document(collection = "books")
class BookDocument(
    @Id
    val id: String? = null,

    val title: String,
    val description: String,
    val publishingYear: Int,
    val pageCount: Int,

    val thumbnailUrl: String,
    val smallThumbnailUrl: String,

    val authors: List<AuthorEmbedded>,
    val genres: List<GenreEmbedded>,
    val ratings: List<RatingEmbedded>
)

data class AuthorEmbedded(
    val id: Long,
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
)

data class RatingSourceEmbedded(
    val id: Int,
    val name: String,
    val url: String
)
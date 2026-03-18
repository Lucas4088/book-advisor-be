package io.github.luksal.book.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
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
    @JsonProperty("ke")
    val key: String,
    val publicId: String,
    @JsonProperty("na")
    val name: String,
    val otherNames: List<String>? = null
)

data class GenreUpdate(
    val id: Long?,
    @JsonProperty("na")
    val name: String
)

data class RatingUpdate(
    @Id
    @JsonProperty("_id")
    val id: String? = null,
    @JsonProperty("sc")
    val score: BigDecimal,
    @JsonProperty("co")
    val count: Int,
    @JsonProperty("so")
    val source: RatingSourceUpdate
)

data class RatingSourceUpdate(
    val id: Int? = null,
    @JsonProperty("na")
    val name: String,
    @JsonProperty("ur")
    val url: String
)
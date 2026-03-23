package io.github.luksal.book.db.document.book

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.luksal.book.db.util.MongoBigDecimalDeserializer
import io.github.luksal.book.db.util.MongoDateDeserializer
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import tools.jackson.databind.annotation.JsonDeserialize
import java.math.BigDecimal
import java.time.LocalDateTime

@Document(collection = "books")
data class BookDocument(
    @Id
    @JsonProperty("_id")
    val id: String,
    @Field("tt")
    @JsonProperty("tt")
    val title: String,
    @Field(name = "des")
    @JsonProperty("des")
    val description: String?,
    @Field("py")
    @JsonProperty("py")
    val publishingYear: Int,
    @Field("pc")
    @JsonProperty("pc")
    val pageCount: Int?,

    @Field("ed")
    @JsonProperty("ed")
    val edition: EditionEmbedded? = null,

    @Field(name = "thu")
    @JsonProperty("thu")
    val thumbnailUrl: String?,
    @Field("stu")
    @JsonProperty("stu")
    val smallThumbnailUrl: String?,

    @Field("au")
    @JsonProperty("au")
    val authors: List<AuthorEmbedded>? = emptyList(),
    @Field("ge")
    @JsonProperty("ge")
    val genres: List<GenreEmbedded>? = emptyList(),
    @Field("ra")
    @JsonProperty("ra")
    val ratings: Set<RatingEmbedded>? = emptySet(),
    @Field("co")
    @JsonProperty("co")
    @JsonDeserialize(using = MongoDateDeserializer::class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdOn: LocalDateTime
)

data class EditionEmbedded(
    @Field("tt")
    @JsonProperty("tt")
    val title: String,
    @Field("la")
    @JsonProperty("la")
    val lang: String
)

data class AuthorEmbedded(
    @JsonProperty("na")
    @Field("na")
    val name: String,
    @JsonProperty("ke")
    @Field("ke")
    val key: String,
    @Field("on")
    @JsonProperty("on")
    val otherNames: List<String>? = emptyList()
)

data class GenreEmbedded(
    @Field("na")
    @JsonProperty("na")
    val name: String
)

data class RatingEmbedded(
    @Id
    @JsonProperty("_id")
    val id: String? = null,
    @Field("sc")
    @JsonDeserialize(using = MongoBigDecimalDeserializer::class)
    @JsonProperty("sc")
    val score: BigDecimal,
    @Field("co")
    @JsonProperty("co")
    val count: Int,
    @Field("so")
    @JsonProperty("so")
    val source: RatingSourceEmbedded,
    @Field("tci")
    @JsonDeserialize(using = MongoBigDecimalDeserializer::class)
    @JsonProperty("tci")
    val titleConfidenceIndicator: BigDecimal,
    @Field("aci")
    @JsonDeserialize(using = MongoBigDecimalDeserializer::class)
    @JsonProperty("aci")
    val authorsConfidenceIndicator: BigDecimal,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RatingEmbedded) return false
        return source.name == other.source.name
    }

    override fun hashCode(): Int = source.name.hashCode()
}

@Document(collection = "ratings")
data class RatingDocument(
    @Id
    @JsonProperty("_id")
    val id: String? = null,
    @Field("bi")
    @JsonProperty("bi")
    val bookId: String,
    @Field("sc")
    @JsonProperty("sc")
    val score: BigDecimal,
    @Field("co")
    @JsonProperty("co")
    val count: Int,
    @Field("so")
    @JsonProperty("so")
    val source: RatingSourceEmbedded,
    @Field("tci")
    @JsonProperty("tci")
    val titleConfidenceIndicator: BigDecimal,
    @Field("aci")
    @JsonProperty("aci")
    val authorsConfidenceIndicator: BigDecimal,
)

data class RatingSourceEmbedded(
    @Field("na")
    @JsonProperty("na")
    val name: String,
    @Field("ur")
    @JsonProperty("ur")
    val url: String
)
package io.github.luksal.integration.source.openlibrary.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.luksal.book.db.document.bookbasicinfo.BookBasicInfoDocument
import io.github.luksal.util.ext.normalizeWhiteChars
import io.github.luksal.util.ext.sha256
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class OpenLibrarySearchResponse(
    val start: Int,
    val numFound: Int,
    val docs: List<OpenLibraryDoc> = emptyList()
)

data class OpenLibraryDoc(
    @JsonProperty("edition_count")
    val editionCount: Int? = null,
    val title: String,
    @JsonProperty("author_name")
    val authorName: List<String>? = null,
    @JsonProperty("first_publish_year")
    val firstPublishYear: Int? = null,
    val key: String,
    @JsonProperty("author_key")
    val authorKey: List<String>? = null,
    val language: List<String>,
    val editions: Editions? = null,

    val openLibraryKey: String? = null,
    val firstPublishDate: String? = null,
    val subjects: List<String> = emptyList(),
    val description: String?,
) {

    fun editionKey(): String? =
        editions?.docs?.firstOrNull()?.key

    @OptIn(ExperimentalUuidApi::class)
    fun toBasicInfoDocument(lang: String) = BookBasicInfoDocument(
        title = title,
        publicId = Uuid.generateV7().toString(),
        openLibraryKey = key,
        openLibraryEditionKey = editionKey(),
        authors = authorName ?: emptyList(),
        firstPublishYear = firstPublishYear,
        subjects = subjects,
        description = description,
        firstPublishDate = firstPublishDate,
        lang = lang
    )
}

data class Editions(
    val numFound: Int?,
    val start: Int?,
    val numFoundExact: Boolean?,
    val docs: List<EditionDoc>?
)

data class EditionDoc(
    val key: String?,
    val title: String?
)
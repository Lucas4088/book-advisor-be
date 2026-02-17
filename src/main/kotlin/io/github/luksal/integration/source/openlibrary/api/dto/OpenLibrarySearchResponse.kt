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
    val editionCount: Int?,
    val title: String,
    @JsonProperty("author_name")
    val authorName: List<String>?,
    @JsonProperty("first_publish_year")
    val firstPublishYear: Int?,
    val key: String,
    @JsonProperty("author_key")
    val authorKey: List<String>?,
    val language: List<String>,
    val editions: Editions?
) {
    fun editionTitle(): String? =
        editions?.docs?.firstOrNull()?.title

    fun editionKey(): String? =
        editions?.docs?.firstOrNull()?.key

    @OptIn(ExperimentalUuidApi::class)
    fun toBasicInfoDocument(lang: String) = BookBasicInfoDocument(
        id = (title + editionTitle()).normalizeWhiteChars().sha256(),
        title = title,
        publicId = Uuid.generateV7().toString(),
        openLibraryKey = key,
        editionTitle = editionTitle(),
        openLibraryEditionKey = editionKey(),
        authors = authorName ?: emptyList(),
        firstPublishYear = firstPublishYear,
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
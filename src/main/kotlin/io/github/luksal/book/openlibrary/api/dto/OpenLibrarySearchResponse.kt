package io.github.luksal.book.openlibrary.api.dto

data class OpenLibrarySearchResponse(
    val start: Int,
    val numFound: Int,
    val docs: List<OpenLibraryDoc> = emptyList()
)

data class OpenLibraryDoc(
    val editionCount: Int?,
    val title: String,
    val authorName: List<String>?,
    val firstPublishYear: Int?,
    val key: String?,
    val authorKey: List<String>?,
    val language: List<String>,
    val editions: Editions?
)

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
package io.github.luksal.book.api.dto


data class BookBasicInfoSearchCriteria(
    val id: Long? = null,
    val bookId: String? = null,
    val title: String? = null,
    val startYear: String? = null,
    val endYear: String? = null,
)

data class BookBasicInfoDto(
    val id: String? = null,
    val bookId: String? = null,
    val title: String? = null,
    val firstPublishDate: String? = null,
)

data class BookBasicInfoDetailsDto(
    val id: String? = null,
    val bookId: String,
    val title: String,
    val openLibraryKey: String? = null,
    val openLibraryEditionKey: String? = null,
    val editionTitle: String = title,
    val firstPublishYear: Int? = null,
    val firstPublishDate: String? = null,
    val authors: List<String> = emptyList(),
    val authorsKeys: List<String> = emptyList(),
    val lang: String?,
    val subjects: List<String> = emptyList(),
    val description: String?,
)

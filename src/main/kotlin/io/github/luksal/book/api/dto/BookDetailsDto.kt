package io.github.luksal.book.api.dto

import io.github.luksal.book.service.dto.BookSearchCriteriaDto

data class BookSearchCriteria(
    val bookId: String? = null,
    val title: String?,
    val genres: List<String>? = null,
    val startYear: Int? = null,
    val endYear: Int? = null,
) {
    fun toServiceDto(): BookSearchCriteriaDto = BookSearchCriteriaDto(
        bookId = bookId,
        title = title,
        genres = genres,
        startYear = startYear,
        endYear = endYear,
    )
}

data class BookDto(
    val id: String? = null,
    val title: String? = null,
    val smallThumbnailUrl: String? = null,
    val publishedYear: Int,
    val rating: BasicRating?
)

data class BookDetailsDto(
    val bookId: String? = null,
    val title: String,
    val authors: List<AuthorDto>,
    val publishedYear: Int,
    val description: String?,
    val genres: List<String>,
    val publishingYear: Int,
    val pageCount: Int?,
    val thumbnailUrl: String?,
    val smallThumbnailUrl: String?,
    val rating: RatingResult?
)


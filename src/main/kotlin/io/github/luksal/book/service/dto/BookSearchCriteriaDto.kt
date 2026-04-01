package io.github.luksal.book.service.dto

data class BookSearchCriteriaDto(
    val bookId: String? = null,
    val title: String? = null,
    val genres: List<String>? = null,
    val startYear: Int? = null,
    val endYear: Int? = null,
)
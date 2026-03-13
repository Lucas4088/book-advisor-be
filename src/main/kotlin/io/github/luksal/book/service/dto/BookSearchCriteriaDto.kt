package io.github.luksal.book.service.dto

import io.github.luksal.book.model.Genre

data class BookSearchCriteriaDto(
    val bookId: String? = null,
    val title: String? = null,
    val genres: List<Genre>? = null,
    val startYear: Int? = null,
    val endYear: Int? = null,
)
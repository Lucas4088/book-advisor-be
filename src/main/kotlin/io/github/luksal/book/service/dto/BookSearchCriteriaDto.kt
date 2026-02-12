package io.github.luksal.book.service.dto

import io.github.luksal.book.model.Genre

data class BookSearchCriteriaDto(
    val title: String? = null,
    val genres: List<Genre>? = null,
    val publishedYearRange: IntRange
)
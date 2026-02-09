package io.github.luksal.book.service.dto

import io.github.luksal.book.model.Genre

data class BookSearchCriteriaDto(
    val title: String?,
    val genres: Set<Genre> = emptySet(),
    val publishedYearRange: IntRange
)
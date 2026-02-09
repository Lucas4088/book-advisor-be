package io.github.luksal.book.dto

data class SearchBookExternalApiCriteriaDto(
    val title: String?,
    val publishedYearRange: IntRange?
)
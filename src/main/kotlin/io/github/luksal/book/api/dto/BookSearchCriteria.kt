package io.github.luksal.book.api.dto

import io.github.luksal.book.model.Genre

data class BookSearchCriteria(
    val title: String,
    val genre: Set<Genre>,
    val publishedYearRange: PublishedYearRange
)

data class PublishedYearRange(
    val from: Int,
    val to: Int
) {
    init {
        require(from <= to) { "PublishedYearRange must be <= from=$from, to=$to" }
    }

    fun toIntRange(): IntRange = from..to
}
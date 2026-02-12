package io.github.luksal.book.api.dto

import io.github.luksal.book.model.Genre
import io.github.luksal.book.service.dto.BookSearchCriteriaDto

data class BookSearchCriteriaRequest(
    val title: String?,
    val genres: List<Genre> = emptyList(),
    val publishedYearRange: PublishedYearRange
) {
    fun toServiceDto(): BookSearchCriteriaDto = BookSearchCriteriaDto(
        title = title,
        genres = genres,
        publishedYearRange = publishedYearRange.toIntRange()
    )
}

data class PublishedYearRange(
    val from: Int = 0,
    val to: Int = Int.MAX_VALUE
) {
    init {
        require(from <= to) { "PublishedYearRange must be <= from=$from, to=$to" }
    }

    fun toIntRange(): IntRange = from..to
}
package io.github.luksal.book.db.document.book

data class BookRatingCountStats(
    val ratingCount: Int,
    val documents: Long,
)
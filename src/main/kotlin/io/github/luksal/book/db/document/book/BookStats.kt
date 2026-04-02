package io.github.luksal.book.db.document.book

data class BookRatingCountStats(
    val ratingCount: Long,
    val documents: Long,
)
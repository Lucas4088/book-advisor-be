package io.github.luksal.book.api.dto

data class BookSearchResponse(
    val id: String,
    val title: String,
    val smallThumbnailUrl: String?
) {
}
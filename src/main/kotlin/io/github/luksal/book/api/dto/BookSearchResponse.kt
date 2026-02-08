package io.github.luksal.book.api.dto

import io.github.luksal.book.model.Book

data class BookSearchResponse(
    val id: Long,
    val title: String,
    val smallThumbnailUrl: String
) {
    fun fromDomain(book: Book): BookSearchResponse =
        BookSearchResponse(book.id, book.title, book.smallThumbnailUrl)

}
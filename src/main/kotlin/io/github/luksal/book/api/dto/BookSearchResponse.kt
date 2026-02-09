package io.github.luksal.book.api.dto

import io.github.luksal.book.db.jpa.model.BookEntity

data class BookSearchResponse(
    val id: Long?,
    val title: String,
    val smallThumbnailUrl: String
) {
    companion object {
        fun fromEntity(book: BookEntity): BookSearchResponse =
            BookSearchResponse(book.id , book.title, book.smallThumbnailUrl)
    }
}
package io.github.luksal.book.api.dto

data class BookSearchResponse(
    val id: String?,
    val title: String,
    val smallThumbnailUrl: String
) {
    /*    companion object {
            fun fromEntity(book: BookEntity): BookSearchResponse =
                BookSearchResponse(book.id , book.title, book.smallThumbnailUrl)
        }*/
}
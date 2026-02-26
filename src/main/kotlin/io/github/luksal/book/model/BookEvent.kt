package io.github.luksal.book.model


data class BookDocumentSavedEvent(
    val bookId: String,
)

data class BookBasicInfoDocumentSavedEvent(
    val bookId: String,
)

data class BookEntitySavedEvent(
    val bookId: String,
)
package io.github.luksal.book.model

import io.github.luksal.event.Event


data class BookDocumentSavedEvent(
    val bookId: String,
) : Event

data class BookBasicInfoDocumentSavedEvent(
    val bookId: String
) : Event

data class BookBasicInfoDocumentEditionSavedEvent(
    val bookId: String,
) : Event

data class BookEntitySavedEvent(
    val bookId: String,
) : Event

data class BookEditionEntitySavedEvent(
    val bookId: String,
) : Event
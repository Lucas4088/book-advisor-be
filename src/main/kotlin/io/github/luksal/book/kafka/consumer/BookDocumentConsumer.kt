package io.github.luksal.book.kafka.consumer

import io.github.luksal.book.db.document.book.BookDocument
import io.github.luksal.book.service.BookService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class BookDocumentConsumer(
    private val bookService: BookService
) {

    @KafkaListener(
        topics = [$$"${app.kafka.connect.books-source-topic}"],
        containerFactory = "bookKafkaListenerContainerFactory")
    fun consume(document: BookDocument) {
        bookService.syncToEntity(document)
    }
}
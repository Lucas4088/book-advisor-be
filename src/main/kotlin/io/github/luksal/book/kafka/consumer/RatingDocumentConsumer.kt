package io.github.luksal.book.kafka.consumer

import io.github.luksal.book.db.document.book.RatingDocument
import io.github.luksal.book.service.RatingService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class RatingDocumentConsumer(
    private val ratingService: RatingService
) {


    @KafkaListener(
        topics = [$$"${app.kafka.connect.ratings-source-topic}"],
        containerFactory = "ratingKafkaListenerContainerFactory"
    )
    fun handle(message: RatingDocument) {
        ratingService.syncToEntity(message)
    }
}
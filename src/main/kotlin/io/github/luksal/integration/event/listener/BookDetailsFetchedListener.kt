package io.github.luksal.integration.event.listener

import io.github.luksal.commons.jpa.EventMeta
import io.github.luksal.integration.db.BookDetailsFetchedEventEntity
import io.github.luksal.integration.db.BookDetailsFetchedEventRepository
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BookDetailsFetchedListener(
    private val eventRepository: BookDetailsFetchedEventRepository,
) {

    @EventListener
    fun handle(event: BookDetailsFetchedEvent) {
        eventRepository.save(
            BookDetailsFetchedEventEntity(
                sourceName = event.sourceName,
                meta = EventMeta(status = event.status)
            )
        )
    }
}
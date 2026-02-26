package io.github.luksal.book.listener

import io.github.luksal.book.db.jpa.event.PopulateBookDetailsEventJpaRepository
import io.github.luksal.book.db.jpa.event.SyncBookEventJpaRepository
import io.github.luksal.book.db.jpa.model.event.PopulateBookDetailsEventEntity
import io.github.luksal.book.db.jpa.model.event.SyncBookEventEntity
import io.github.luksal.book.model.BookBasicInfoDocumentSavedEvent
import io.github.luksal.book.model.BookDocumentSavedEvent
import io.github.luksal.book.model.BookEntitySavedEvent
import io.github.luksal.ingestion.crawler.jpa.ScheduledBookCrawlerEventJpa
import io.github.luksal.ingestion.crawler.jpa.entity.ScheduledBookCrawlerEventEntity
import io.github.luksal.ingestion.crawler.service.PageCrawlerCrudService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BookEventListener(
    private val syncBookEventJpaRepository: SyncBookEventJpaRepository,
    private val populateBookDetailsEventJpaRepository: PopulateBookDetailsEventJpaRepository,
    private val crawlerEventJpa: ScheduledBookCrawlerEventJpa,
    private val crawlerCrudService: PageCrawlerCrudService
) {

    @EventListener
    fun handle(event: BookDocumentSavedEvent) =
        syncBookEventJpaRepository.save(SyncBookEventEntity(bookId = event.bookId))

    @EventListener
    fun handle(event: BookEntitySavedEvent) =
        crawlerCrudService.findAll()
            .forEach {
                crawlerEventJpa.save(ScheduledBookCrawlerEventEntity(bookId = event.bookId, crawlerId = it.id!!))
            }

    @EventListener
    fun handle(event: BookBasicInfoDocumentSavedEvent) =
        populateBookDetailsEventJpaRepository.save(PopulateBookDetailsEventEntity(bookId = event.bookId))

}
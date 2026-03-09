package io.github.luksal.book.service

import io.github.luksal.book.db.jpa.event.PopulateBookBasicDataJpaRepository
import io.github.luksal.book.db.jpa.event.PopulateBookDetailsEventJpaRepository
import io.github.luksal.book.db.jpa.event.SyncBookEventJpaRepository
import io.github.luksal.ingestion.crawler.jpa.ScheduledBookCrawlerEventRepository
import jakarta.transaction.Transactional
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service

@Service
class BookDataPurgeService(
    private val bookService: BookService,
    private val bookBasicDataPopulationJpaRepository: PopulateBookBasicDataJpaRepository,
    private val populateBookDetailsEventJpaRepository: PopulateBookDetailsEventJpaRepository,
    private val scheduledBookCrawlerEventRepository: ScheduledBookCrawlerEventRepository,
    private val syncBookEventJpaRepository: SyncBookEventJpaRepository
) {

    @Transactional
    fun purgeBooks() {
        bookService.deleteAllBookDetails()
        scheduledBookCrawlerEventRepository.deleteAll()
        syncBookEventJpaRepository.deleteAll()
    }

    @Transactional
    fun purgeBookBasicInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            bookService.deleteAllBookBasicInfo()
            bookBasicDataPopulationJpaRepository.deleteAll()
            populateBookDetailsEventJpaRepository.deleteAll()
        }
    }
}
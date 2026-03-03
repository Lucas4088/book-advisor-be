package io.github.luksal.ingestion.service

import io.github.luksal.book.model.BookUpdate
import io.github.luksal.book.service.BookService
import io.github.luksal.ingestion.crawler.job.PageCrawlerScheduledJob.Companion.log
import io.github.luksal.ingestion.crawler.jpa.ScheduledBookCrawlerEventRepository
import io.github.luksal.ingestion.crawler.jpa.entity.ScheduledBookCrawlerEventEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookIngestionService(
    private val crawlerEventRepository: ScheduledBookCrawlerEventRepository,
    private val bookService: BookService,
    private val bookPageCrawlerService: BookPageCrawlerService
) {

    @Transactional
    fun crawlForRating(crawlerId: Long) =
        crawlerEventRepository.claimPending(5, crawlerId)?.forEach {
            crawlAndSaveRating(it.crawlerId, it)
        }

    private fun crawlAndSaveRating(
        key: Long,
        value: ScheduledBookCrawlerEventEntity
    ) = runCatching {
        value.meta.markAsInProgress()
        crawlForRating(key, value.bookId)
    }.onFailure {
        log.error("Error crawling book with id ${value.bookId} using crawler ${value.crawlerId}", it)
        value.meta.markAsFailed(it.message ?: "Unknown error")
    }.onSuccess {
        value.meta.markAsSuccess()
    }.let {
        crawlerEventRepository.save(value)
    }

    private fun crawlForRating(crawlerId: Long, bookId: String) =
        bookService.getBookById(bookId).let { book ->
            bookPageCrawlerService.crawlBookPage(crawlerId, book)?.let {
                bookService.updateBook(BookUpdate(id = book.id, ratings = listOf(it)))
            }
        }

}
package io.github.luksal.ingestion.service

import io.github.luksal.book.db.document.book.RatingDocument
import io.github.luksal.book.db.document.book.RatingSourceEmbedded
import io.github.luksal.book.db.document.rating.repository.RatingDocumentRepository
import io.github.luksal.book.model.BookUpdate
import io.github.luksal.book.service.BookService
import io.github.luksal.commons.dto.EventStatus
import io.github.luksal.ingestion.crawler.job.PageCrawlerScheduledJob.Companion.log
import io.github.luksal.ingestion.crawler.jpa.ScheduledBookCrawlerEventRepository
import io.github.luksal.ingestion.crawler.jpa.entity.ScheduledBookCrawlerEventEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class BookRatingIngestionService(
    private val crawlerEventRepository: ScheduledBookCrawlerEventRepository,
    private val bookService: BookService,
    private val ratingDocumentRepository: RatingDocumentRepository,
    private val bookPageCrawlerService: BookPageCrawlerService
) {

    @Transactional
    fun crawlForRating(eventStatus: EventStatus, crawlerId: Long) =
        crawlerEventRepository.claimByStatus(eventStatus, 5, crawlerId)?.forEach {
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
        value.markAsFailed(it.message ?: "Unknown error")
        value.incrementRetryCount()
    }.onSuccess {
        value.meta.markAsSuccess()
    }.let {
        crawlerEventRepository.save(value)
    }

    private fun crawlForRating(crawlerId: Long, bookId: String) =
        bookService.getBookByIdForCrawling(bookId).let { book ->
            bookPageCrawlerService.crawlBookPage(crawlerId, book)?.takeIf { it.score != BigDecimal.ZERO }?.let {
                ratingDocumentRepository.save(
                    RatingDocument(
                        bookId = book.id,
                        score = it.score,
                        count = it.count,
                        source = RatingSourceEmbedded(
                            name = it.source.name,
                            url = it.source.url
                        ),
                        titleConfidenceIndicator = it.titleConfidenceIndicator,
                        authorsConfidenceIndicator = it.authorsConfidenceIndicator,
                    )
                )
                bookService.updateBook(BookUpdate(id = book.id, ratings = listOf(it)))
            }
        }

}
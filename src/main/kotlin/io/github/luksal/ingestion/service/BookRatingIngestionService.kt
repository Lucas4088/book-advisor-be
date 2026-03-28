package io.github.luksal.ingestion.service

import io.github.luksal.book.db.document.book.RatingDocument
import io.github.luksal.book.db.document.book.RatingSourceEmbedded
import io.github.luksal.book.db.document.rating.repository.RatingDocumentRepository
import io.github.luksal.book.model.BookUpdate
import io.github.luksal.book.service.BookService
import io.github.luksal.commons.dto.EventStatus
import io.github.luksal.ingestion.crawler.job.PageCrawlerScheduledJob.Companion.log
import io.github.luksal.ingestion.crawler.jpa.ScheduledBookCrawlerEventRepository
import io.github.luksal.ingestion.crawler.jpa.ScheduledBookCrawlerOnDemandEventRepository
import io.github.luksal.ingestion.crawler.jpa.entity.BaseScheduledBookCrawlerEventEntity
import io.github.luksal.ingestion.crawler.jpa.entity.ScheduledBookCrawlerEventEntity
import io.github.luksal.ingestion.crawler.jpa.entity.ScheduledBookCrawlerOnDemandEventEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class BookRatingIngestionService(
    private val crawlerEventRepository: ScheduledBookCrawlerEventRepository,
    private val crawlerOnDemandEventRepository: ScheduledBookCrawlerOnDemandEventRepository,
    private val scheduledBookCrawlerOnDemandEventRepository: ScheduledBookCrawlerOnDemandEventRepository,
    private val bookService: BookService,
    private val ratingDocumentRepository: RatingDocumentRepository,
    private val bookPageCrawlerService: BookPageCrawlerService
) {


    @Transactional
    fun crawlForRating(eventStatus: EventStatus, crawlerId: Long) =
        crawlerEventRepository.claimByStatus(eventStatus, 5, crawlerId)?.forEach { it ->
            crawlAndSaveRating(it.crawlerId, it).also {
                crawlerEventRepository.save(it as ScheduledBookCrawlerEventEntity)
            }
        }

    @Transactional
    fun crawlForRatingOnDemand(eventStatus: EventStatus, crawlerId: Long) =
        crawlerOnDemandEventRepository.claimByStatus(eventStatus, 5, crawlerId)?.forEach {  it ->
            crawlAndSaveRating(it.crawlerId, it)
                .also {
                    crawlerOnDemandEventRepository.save(it as ScheduledBookCrawlerOnDemandEventEntity)
                }
        }

    @Transactional
    fun scheduleOnDemandCrawling(bookId: String, crawlerId: Long) {
        scheduledBookCrawlerOnDemandEventRepository.findByBookIdAndCrawlerId(
            bookId, crawlerId
        )?.also { return }
        ScheduledBookCrawlerOnDemandEventEntity(
            bookId, crawlerId
        ).let {
            crawlerOnDemandEventRepository.save(it)
        }
    }

    private fun crawlAndSaveRating(
        key: Long,
        value: BaseScheduledBookCrawlerEventEntity
    ): BaseScheduledBookCrawlerEventEntity = runCatching {
        value.meta.markAsInProgress()
        crawlForRating(key, value.bookId, value)
    }.onFailure {
        log.error("Error crawling book with id ${value.bookId} using crawler ${value.crawlerId}", it)
        value.markAsFailed(it.message ?: "Unknown error")
        value.incrementRetryCount()
    }.let {
        value
    }

    private fun crawlForRating(crawlerId: Long, bookId: String, event: BaseScheduledBookCrawlerEventEntity) =
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
                event.meta.markAsSuccess()
        }

}
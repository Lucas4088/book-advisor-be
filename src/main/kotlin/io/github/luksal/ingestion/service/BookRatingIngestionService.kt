package io.github.luksal.ingestion.service

import io.github.luksal.book.db.document.rating.repository.RatingDocumentRepository
import io.github.luksal.book.mapper.BookMapper.toDocument
import io.github.luksal.book.model.BookUpdate
import io.github.luksal.book.service.BookService
import io.github.luksal.commons.dto.EventStatus
import io.github.luksal.ingestion.crawler.job.PageCrawlerScheduledJob.Companion.log
import io.github.luksal.ingestion.crawler.jpa.ScheduledBookCrawlerEventRepository
import io.github.luksal.ingestion.crawler.jpa.ScheduledBookCrawlerOnDemandEventRepository
import io.github.luksal.ingestion.crawler.jpa.entity.BaseScheduledBookCrawlerEventEntity
import io.github.luksal.ingestion.crawler.jpa.entity.ScheduledBookCrawlerOnDemandEventEntity
import io.github.luksal.ingestion.crawler.service.PageCrawlerCrudService
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.math.BigDecimal
import java.time.Instant
import kotlin.random.Random

@Service
class BookRatingIngestionService(
    private val pageCrawlerCrudService: PageCrawlerCrudService,
    private val crawlersTaskSchedulerMap: Map<Long, TaskScheduler>,
    private val crawlerEventRepository: ScheduledBookCrawlerEventRepository,
    private val crawlerOnDemandEventRepository: ScheduledBookCrawlerOnDemandEventRepository,
    private val scheduledBookCrawlerOnDemandEventRepository: ScheduledBookCrawlerOnDemandEventRepository,
    private val bookService: BookService,
    private val ratingDocumentRepository: RatingDocumentRepository,
    private val bookPageCrawlerService: BookPageCrawlerService,
    transactionManager: PlatformTransactionManager
) {

    private val transactionTemplate = TransactionTemplate(transactionManager)

    @Transactional
    fun processScheduledCrawlerTasks(eventStatus: EventStatus) =
        processScheduledCrawlerTasks(
            eventStatus,
            120,
            crawlerEventRepository::claimByStatus,
            crawlerEventRepository::save
        )

    @Transactional
    fun processScheduledCrawlerOnDemandTasks(eventStatus: EventStatus) =
        processScheduledCrawlerTasks(
            eventStatus,
            3,
            crawlerOnDemandEventRepository::claimByStatus,
            crawlerOnDemandEventRepository::save
        )

    private fun <T : BaseScheduledBookCrawlerEventEntity> processScheduledCrawlerTasks(
        eventStatus: EventStatus,
        delay: Long,
        fetchTasks: (EventStatus, Int, Long) -> List<T>?,
        saveTask: (T) -> T
    ) {
        pageCrawlerCrudService.findAll().filter { crawler -> crawler.enabled }.forEach { crawler ->
            fetchTasks.invoke(eventStatus, 5, crawler.id!!)?.forEach { event ->
                event.meta.markAsInProgress()
                saveTask.invoke(event)
                crawlersTaskSchedulerMap[event.crawlerId]?.apply {
                    val delaySeconds = Random.nextLong(1, delay) // 1–120
                    val nextExecutionTime = Instant.ofEpochMilli(System.currentTimeMillis() + delaySeconds * 1000)
                    schedule(
                        {
                            transactionTemplate.execute {
                                crawlAndSaveRating(event.crawlerId, event)
                                    .also {
                                        saveTask.invoke(it)
                                    }
                            }
                        },
                        nextExecutionTime
                    )
                }.also { ts ->
                    val scheduler = ts as ThreadPoolTaskScheduler
                    val executor = scheduler.scheduledExecutor as java.util.concurrent.ScheduledThreadPoolExecutor
                    log.info(
                        "Thread Scheduler info: ${scheduler.threadNamePrefix} : queueSize ${executor.queue.size} " +
                                ": completed ${executor.completedTaskCount}: active ${scheduler.activeCount}"
                    )
                }
            }
        }
    }

    @Transactional
    fun scheduleOnDemandCrawling(bookId: String, crawlerId: Long) {
        scheduledBookCrawlerOnDemandEventRepository.findByBookIdAndCrawlerId(
            bookId, crawlerId
        )?.also { return }
        ScheduledBookCrawlerOnDemandEventEntity(
            bookId, crawlerId
        ).also {
            crawlerOnDemandEventRepository.save(it)
        }
    }

    private fun <T : BaseScheduledBookCrawlerEventEntity> crawlAndSaveRating(
        key: Long,
        value: T
    ): T = runCatching {
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
                ratingDocumentRepository.save(it.toDocument(book.id))
                bookService.updateBookDocument(BookUpdate(id = book.id, ratings = listOf(it)))
            }
            event.meta.markAsSuccess()
        }

}
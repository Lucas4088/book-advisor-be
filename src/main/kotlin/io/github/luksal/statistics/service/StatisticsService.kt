package io.github.luksal.statistics.service

import io.github.luksal.book.db.document.author.repository.AuthorDocumentRepository
import io.github.luksal.book.db.document.book.repository.BookDocumentRepository
import io.github.luksal.book.db.document.bookbasicinfo.repository.BookBasicInfoDocumentRepository
import io.github.luksal.book.db.jpa.BookJpaRepository
import io.github.luksal.ingestion.crawler.jpa.ScheduledBookCrawlerEventRepository
import io.github.luksal.ingestion.crawler.service.PageCrawlerCrudService
import io.github.luksal.integration.db.BookDetailsFetchedEventRepository
import io.github.luksal.statistics.api.dto.*
import io.github.luksal.util.ext.logger
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.function.Supplier

@Service
class StatisticsService(
    private val authorDocumentRepository: AuthorDocumentRepository,
    private val bookBasicInfoDocumentRepository: BookBasicInfoDocumentRepository,
    private val bookDocumentRepository: BookDocumentRepository,
    private val bookJpaRepository: BookJpaRepository,
    private val scheduledBookCrawlerEventRepository: ScheduledBookCrawlerEventRepository,
    private val crawlerCrudService: PageCrawlerCrudService,
    private val bookDetailsFetchedEventRepository: BookDetailsFetchedEventRepository,
) {

    companion object {
        private val log = logger()
    }


    fun calculateBookStatistics(): BookStatisticsDto {
        val bookDocCount = calculateExecTime("book doc count") { bookDocumentRepository.count() }
        val bookCount = calculateExecTime("book count") { bookJpaRepository.count() }
        val bookSyncPercentage =
            if (bookDocCount != 0L)
                bookCount.toBigDecimal()
                    .divide(bookDocCount.toBigDecimal(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
            else BigDecimal.ZERO
        return BookStatisticsDto(
            authorCount = calculateExecTime("author count") { authorDocumentRepository.countApprox() },
            bookBasicInfoCount = calculateExecTime("book basic info") { bookBasicInfoDocumentRepository.countApprox() },
            bookDocumentCount = bookDocCount,
            bookRecordCount = bookCount,
            bookSyncPercentage = bookSyncPercentage
        )

    }

    fun calculateCrawlerEventStatistics(): RatingEventStatusStatisticsDto {
        val crawlers = crawlerCrudService.findAll().associate { it.id to it.name }
        val crawlerEventStatusStats = scheduledBookCrawlerEventRepository.countByStatusAndCrawler()
        return RatingEventStatusStatisticsDto(
            total = crawlerEventStatusStats.map { it.count }.takeIf { it.isNotEmpty() }
                ?.reduce { acc, count -> acc + count } ?: 0,
            values = crawlerEventStatusStats.map {
                RatingEventValueDto(
                    it.eventStatus.name,
                    crawlerName = crawlers[it.crawlerId],
                    it.count
                )
            }
        )
    }

    fun calculateBooksRatingStatistics(): BookRatingStatisticsDto {
        val result = bookDocumentRepository.countBooksPerRatingNumber()
        return BookRatingStatisticsDto(
            totalRatings = result.takeIf { it.isNotEmpty() }?.map { it.documents }?.reduce { acc, total -> acc + total }
                ?: 0,
            values = result.map {
                BookRatingValueDto(
                    numberOfRatings = it.ratingCount,
                    value = it.documents
                )
            }
        )
    }

    fun calculateBookDetailsFetchedStatistics(): BookDetailsFetchedStatistics {
        return bookDetailsFetchedEventRepository.countBySource()
            .filter { it.status != null && it.value != null }
            .groupBy({ Triple(it.bucket, it.sourceName, it.status) }, { it.value!! })
            .mapValues { (_, values) ->
                values.sum()
            }.entries
            .groupBy { it.key.first }
            .map { (bucket, entries) ->
                val map = mutableMapOf<String, Any>(
                    "time" to bucket
                )

                entries.forEach {
                    val (_, source, status) = it.key
                    val key = "${source}_${status}"
                    map[key] = it.value
                }

                map
            }.let {
                BookDetailsFetchedStatistics(it)
            }
    }

    private fun <K> calculateExecTime(name: String, func: Supplier<K>): K {
        val start = Instant.now()
        val result = func.get()
        val end = Instant.now()
        val duration = Duration.between(start, end)
        log.info("$name : ${duration.get(ChronoUnit.NANOS)}\n")
        return result
    }
}
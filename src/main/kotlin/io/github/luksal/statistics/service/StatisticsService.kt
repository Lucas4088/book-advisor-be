package io.github.luksal.statistics.service

import io.github.luksal.book.db.document.author.repository.AuthorDocumentRepository
import io.github.luksal.book.db.document.book.repository.BookDocumentRepository
import io.github.luksal.book.db.document.bookbasicinfo.repository.BookBasicInfoDocumentRepository
import io.github.luksal.book.db.jpa.BookJpaRepository
import io.github.luksal.book.db.jpa.event.PopulateBookDetailsEventJpaRepository
import io.github.luksal.ingestion.crawler.jpa.ScheduledBookCrawlerEventRepository
import io.github.luksal.ingestion.crawler.service.PageCrawlerCrudService
import io.github.luksal.statistics.api.dto.BookStatisticsDto
import io.github.luksal.statistics.api.dto.RatingEventStatusStatisticsDto
import io.github.luksal.statistics.api.dto.RatingEventValueDto
import org.springframework.stereotype.Service

@Service
class StatisticsService(
    private val authorDocumentRepository: AuthorDocumentRepository,
    private val bookBasicInfoDocumentRepository: BookBasicInfoDocumentRepository,
    private val bookDocumentRepository: BookDocumentRepository,
    private val bookJpaRepository: BookJpaRepository,
    private val ratingBookDetailsEventJpaRepository: PopulateBookDetailsEventJpaRepository,
    private val scheduledBookCrawlerEventRepository: ScheduledBookCrawlerEventRepository,
    private val crawlerCrudService: PageCrawlerCrudService
) {


    fun calculateBookStatistics(): BookStatisticsDto {
        val bookDocCount = bookDocumentRepository.count()
        val bookCount = bookJpaRepository.count()
        return BookStatisticsDto(
            authorCount = authorDocumentRepository.count(),
            bookBasicInfoCount = bookBasicInfoDocumentRepository.count(),
            bookDocumentCount = bookDocumentRepository.count(),
            bookRecordCount = bookCount,
            bookSyncPercentage = (bookCount.toDouble()) / bookDocCount
        )

    }

    /*
       fun calculateRatingCrawlerStatistics(): RatingStatisticsDto {
            return RatingStatisticsDto(0, )
        }
    */

    fun calculateCrawlerEventStatistics(): RatingEventStatusStatisticsDto {
        val crawlers = crawlerCrudService.findAll().associate { it.id to it.name }
        val crawlerEventStatusStats = scheduledBookCrawlerEventRepository.countByStatusAndCrawler()
        return RatingEventStatusStatisticsDto(
            total = crawlerEventStatusStats.map { it.count }.reduce { acc, count -> acc + count },
            values = crawlerEventStatusStats.map {
                RatingEventValueDto(
                    it.eventStatus.name,
                    crawlerName = crawlers[it.crawlerId],
                    it.count
                )
            }
        )
    }
}
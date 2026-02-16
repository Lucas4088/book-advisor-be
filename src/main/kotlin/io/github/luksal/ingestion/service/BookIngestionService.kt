package io.github.luksal.ingestion.service

import io.github.luksal.book.model.Rating
import io.github.luksal.book.model.RatingSource
import io.github.luksal.book.service.BookService
import io.github.luksal.book.service.dto.BookSearchCriteriaDto
import io.github.luksal.config.CrawlerProperties
import io.github.luksal.config.ScrapingProxyProperties
import io.github.luksal.ingestion.crawler.PageCrawler
import io.github.luksal.ingestion.fetcher.HttpFetcher
import io.github.luksal.util.ext.logger
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.net.URLEncoder

@Service
class BookIngestionService(
    private val fetcher: HttpFetcher,
    private val pageCrawler: PageCrawler,
    private val bookService: BookService,
    private val crawlerProperties: CrawlerProperties,
    private val scrapingProxyProperties: ScrapingProxyProperties
) {

    val log = logger()

    //TODO split into mathods and component to make it more fault tolerant and testable
    //TODO worker per crawler source???
    fun crawlAndIngest() {
        bookService.searchBooks(
            BookSearchCriteriaDto(publishedYearRange = 2000..2024),
            PageRequest.of(0, 20)
        ).forEach { book ->
            log.info("Crawling for book: ${book.title}")
            crawlerProperties.crawlers.forEach { crawlerSpec ->
                val searchTitle = book.title/*.replace(" ", crawlerSpec.path.titleSpaceSeparator)*/
                    .let { URLEncoder.encode(it, "UTF-8") }
                val searchPath = crawlerSpec.path.search.replace("{formattedTitle}", searchTitle)
                val searchUrl = "${crawlerSpec.baseUrl}$searchPath"
                val proxiedSearchUrl = scrapingProxyProperties.url + "?url=" + "${crawlerSpec.baseUrl}${searchPath}"
                val fetchUrl = if (crawlerSpec.proxyEnabled) proxiedSearchUrl else searchUrl

                val searchPageHtml = fetcher.fetch(fetchUrl)

                val bookPage = pageCrawler.extractBookPageLink(searchPageHtml, crawlerSpec)?.let {
                    log.info(it)
                    val bookDetailsPage = fetcher.fetch(it)
                    val rating = Rating(
                        0,
                        pageCrawler.extractRatingScore(bookDetailsPage, crawlerSpec) ?: BigDecimal.ZERO,
                        pageCrawler.extractRatingCount(bookDetailsPage, crawlerSpec) ?: 0,
                        RatingSource(0, crawlerSpec.name, it)
                    )
                    log.info(rating.toString())
                }

            }
        }
    }
}
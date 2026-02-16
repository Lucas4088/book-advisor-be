package io.github.luksal.ingestion.service

import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.model.Rating
import io.github.luksal.book.model.RatingSource
import io.github.luksal.book.service.BookService
import io.github.luksal.book.service.dto.BookSearchCriteriaDto
import io.github.luksal.config.CrawlerProperties
import io.github.luksal.config.CrawlerSpecification
import io.github.luksal.config.ScrapingProxyProperties
import io.github.luksal.ingestion.crawler.PageCrawler
import io.github.luksal.ingestion.fetcher.PageFetcher
import io.github.luksal.util.ext.logger
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.net.URLEncoder

@Service
class BookIngestionService(
    private val pageFetcher: PageFetcher,
    private val pageCrawler: PageCrawler,
    private val bookService: BookService,
    private val crawlerProperties: CrawlerProperties,
    private val scrapingProxyProperties: ScrapingProxyProperties
) {

    private val log = logger()

    companion object {
        private const val FORMATTED_TITLE_PLACEHOLDER = "{formattedTitle}"
    }

    //TODO split into mathods and component to make it more fault tolerant and testable
    //TODO worker per crawler source???
    fun crawlAndIngest() {
        bookService.searchBooks(
            BookSearchCriteriaDto(publishedYearRange = 2000..2024),
            PageRequest.of(0, 20)
        ).forEach { book ->
            log.info("Crawling for book: ${book.title}")
            crawlerProperties.crawlers.filter { it.enabled }.forEach { crawlerSpec ->
                val searchUrl = composeSearchBookUrl(book, crawlerSpec)
                log.info("Composed search url: $searchUrl")
                val searchPageHtml = pageFetcher.fetch(searchUrl)

                pageCrawler.extractBookPageUrl(searchPageHtml, crawlerSpec)?.let {
                    log.info("Found book page url: $it")
                    pageFetcher.fetch(it).takeIf { bookPage -> bookPage.isNotEmpty() }?.let { bookPage ->
                        createAndSaveRating(bookPage, crawlerSpec, book)
                    }
                }
            }
        }
    }

    private fun composeSearchBookUrl(book: BookSearchResponse, crawlerSpec: CrawlerSpecification): String {
        val searchTitle = book.title
            .let { URLEncoder.encode(it, Charsets.UTF_8) }
        val searchPath = crawlerSpec.path.search.replace(FORMATTED_TITLE_PLACEHOLDER, searchTitle)
        val searchUrl = "${crawlerSpec.baseUrl}$searchPath"
        val proxiedSearchUrl = scrapingProxyProperties.url + "?url=" + "${crawlerSpec.baseUrl}${searchPath}"
        return if (crawlerSpec.proxyEnabled) proxiedSearchUrl else searchUrl
    }

    private fun createAndSaveRating(
        bookPage: String,
        crawlerSpec: CrawlerSpecification,
        book: BookSearchResponse
    ) {
        val rating = Rating(
            score = pageCrawler.extractRatingScore(bookPage, crawlerSpec) ?: BigDecimal.ZERO,
            count = pageCrawler.extractRatingCount(bookPage, crawlerSpec) ?: 0,
            source = RatingSource(name = crawlerSpec.name, url = crawlerSpec.baseUrl)
        )
        bookService.updateBookRating(book.id, rating)
        log.info("Extracted rating for book ${book.title} from source ${crawlerSpec.name}: $rating")
    }
}
package io.github.luksal.ingestion.service

import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.model.BookUpdate
import io.github.luksal.book.model.RatingSourceUpdate
import io.github.luksal.book.model.RatingUpdate
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
        bookService.searchBookDocuments(
            BookSearchCriteriaDto(publishedYearRange = 2000..2024),
            PageRequest.of(0, 20)
        ).forEach { book ->
            crawlerProperties.crawlers.filter { it.enabled }.forEach { crawlerSpec ->
                val searchUrl = composeSearchBookUrl(book, crawlerSpec)
                log.info("Composed search url: $searchUrl")
                val searchPageHtml = pageFetcher.fetch(searchUrl)

                pageCrawler.extractBookPageUrl(searchPageHtml, crawlerSpec)?.let {
                    val pageUrl = composeBookPageUrl(it, crawlerSpec)
                    log.info("Composed page url: $pageUrl")
                    pageFetcher.fetch(composeBookPageUrl(it, crawlerSpec)).takeIf { bookPage -> bookPage.isNotEmpty() }?.let { bookPage ->
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
        val proxiedSearchUrl = "${scrapingProxyProperties.url}?url=${crawlerSpec.baseUrl}${searchPath}"
        //TODO missing book page for amazon - eg Dracula
        /*2026-02-19T15:50:48.472+01:00  INFO 24248 --- [nio-8090-exec-1] i.g.l.i.service.BookIngestionService     : Composed search url: http://localhost:8000/html?url=https://www.amazon.com/s?k=Dracula&i=stripbooks
        2026-02-19T15:50:48.472+01:00  INFO 24248 --- [nio-8090-exec-1] i.g.l.i.fetcher.PageFetcher$Companion    : Fetching http://localhost:8000/html?url=https://www.amazon.com/s?k=Dracula&i=stripbooks
        2026-02-19T15:50:58.259+01:00  INFO 24248 --- [nio-8090-exec-1] i.g.l.i.service.BookIngestionService     : Composed page url: http://localhost:8000/html?url=https://www.amazon.com#
        2026-02-19T15:50:58.259+01:00  INFO 24248 --- [nio-8090-exec-1] i.g.l.i.fetcher.PageFetcher$Companion    : Fetching http://localhost:8000/html?url=https://www.amazon.com#
        2026-02-19T15:51:08.177+01:00  INFO 24248 --- [nio-8090-exec-1] i.g.l.i.service.BookIngestionService     : Extracted rating for book Dracula from source amazon-books: RatingUpdate(id=null, score=0, count=0, source=RatingSourceUpdate(id=null, name=amazon-books, url=https://www.amazon.com))*/
        return if (crawlerSpec.proxyEnabled) proxiedSearchUrl else searchUrl
    }

    private fun composeBookPageUrl(pageUrl: String, crawlerSpec: CrawlerSpecification): String {
        val proxiedSearchUrl = "${scrapingProxyProperties.url}?url=${pageUrl}"
        return if (crawlerSpec.proxyEnabled) proxiedSearchUrl else pageUrl
    }

    private fun createAndSaveRating(
        bookPage: String,
        crawlerSpec: CrawlerSpecification,
        book: BookSearchResponse
    ) {
        val rating = RatingUpdate(
            score = pageCrawler.extractRatingScore(bookPage, crawlerSpec) ?: BigDecimal.ZERO,
            count = pageCrawler.extractRatingCount(bookPage, crawlerSpec) ?: 0,
            source = RatingSourceUpdate(name = crawlerSpec.name, url = crawlerSpec.baseUrl)
        )
        bookService.updateBook(BookUpdate(id = book.id, ratings = listOf(rating)))
        log.info("Extracted rating for book ${book.title} from source ${crawlerSpec.name}: $rating")
    }
}
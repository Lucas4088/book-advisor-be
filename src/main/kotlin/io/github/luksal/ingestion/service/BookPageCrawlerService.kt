package io.github.luksal.ingestion.service

import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.model.RatingSourceUpdate
import io.github.luksal.book.model.RatingUpdate
import io.github.luksal.config.CrawlerSpecification
import io.github.luksal.config.ScrapingProxyProperties
import io.github.luksal.ingestion.crawler.jpa.PageCrawlerJpaRepository
import io.github.luksal.ingestion.crawler.service.PageCrawler
import io.github.luksal.ingestion.fetcher.PageFetcher
import io.github.luksal.ingestion.mappper.IngestionMapper
import io.github.luksal.util.ext.logger
import org.springframework.stereotype.Service
import java.net.URLEncoder

@Service
class BookPageCrawlerService(
    private val pageCrawlerRepository: PageCrawlerJpaRepository,
    private val pageFetcher: PageFetcher,
    private val pageCrawler: PageCrawler,
    private val scrapingProxyProperties: ScrapingProxyProperties
) {

    private val log = logger()

    companion object {
        private const val FORMATTED_TITLE_PLACEHOLDER = "{formattedTitle}"
    }

    fun crawlBookPage(crawlerId: Long, book: BookSearchResponse): RatingUpdate? {
        return pageCrawlerRepository.findById(crawlerId).orElseThrow()
            .takeIf { it.enabled }
            ?.let {
                crawlAndExtractRating(book, IngestionMapper.map(it))
            }
    }

    private fun crawlAndExtractRating(
        book: BookSearchResponse,
        crawlerSpec: CrawlerSpecification
    ): RatingUpdate? {
        val searchUrl = composeSearchBookUrl(book, crawlerSpec)
        log.info("Composed search url: $searchUrl")
        val searchPageHtml = pageFetcher.fetch(searchUrl)

        return pageCrawler.extractBookPageUrl(searchPageHtml, crawlerSpec)?.let {
            //val pageUrl = composeBookPageUrl(it, crawlerSpec)
            log.info("Composed page url: $it")
            pageFetcher.fetch(it).takeIf { bookPage -> bookPage.isNotEmpty() }
                ?.let { bookPage ->
                    extractRating(bookPage, crawlerSpec, book)
                }
        }
    }

    private fun composeSearchBookUrl(book: BookSearchResponse, crawlerSpec: CrawlerSpecification): String {
        val searchTitle = book.title
            .let { URLEncoder.encode(it, Charsets.UTF_8) }
        val searchPath = crawlerSpec.path.search.replace(FORMATTED_TITLE_PLACEHOLDER, searchTitle)
        val searchUrl = "${crawlerSpec.baseUrl}$searchPath"
        //val proxiedSearchUrl = "${scrapingProxyProperties.url}?url=${crawlerSpec.baseUrl}${searchPath}"
        //val proxiedSearchUrl = scrapingProxyProperties.url
        //TODO missing book page for amazon - eg Dracula
        /*2026-02-19T15:50:48.472+01:00  INFO 24248 --- [nio-8090-exec-1] i.g.l.i.service.BookIngestionService     : Composed search url: http://localhost:8000/html?url=https://www.amazon.com/s?k=Dracula&i=stripbooks
        2026-02-19T15:50:48.472+01:00  INFO 24248 --- [nio-8090-exec-1] i.g.l.i.fetcher.PageFetcher$Companion    : Fetching http://localhost:8000/html?url=https://www.amazon.com/s?k=Dracula&i=stripbooks
        2026-02-19T15:50:58.259+01:00  INFO 24248 --- [nio-8090-exec-1] i.g.l.i.service.BookIngestionService     : Composed page url: http://localhost:8000/html?url=https://www.amazon.com#
        2026-02-19T15:50:58.259+01:00  INFO 24248 --- [nio-8090-exec-1] i.g.l.i.fetcher.PageFetcher$Companion    : Fetching http://localhost:8000/html?url=https://www.amazon.com#
        2026-02-19T15:51:08.177+01:00  INFO 24248 --- [nio-8090-exec-1] i.g.l.i.service.BookIngestionService     : Extracted rating for book Dracula from source amazon-books: RatingUpdate(id=null, score=0, count=0, source=RatingSourceUpdate(id=null, name=amazon-books, url=https://www.amazon.com))*/
        return searchUrl
    }

/*    private fun composeBookPageUrl(pageUrl: String, crawlerSpec: CrawlerSpecification): String {
        //val proxiedSearchUrl = "${scrapingProxyProperties.url}?url=${pageUrl}"
        //return if (crawlerSpec.proxyEnabled) proxiedSearchUrl else pageUrl
    }*/

    private fun extractRating(
        bookPage: String,
        crawlerSpec: CrawlerSpecification,
        book: BookSearchResponse
    ): RatingUpdate? {
        val ratingScore = pageCrawler.extractRatingScore(bookPage, crawlerSpec)
        val ratingCount = pageCrawler.extractRatingCount(bookPage, crawlerSpec)

        return ratingScore?.let {
            RatingUpdate(
                score = it,
                count = ratingCount ?: 0,
                source = RatingSourceUpdate(name = crawlerSpec.name, url = crawlerSpec.baseUrl)
            )
        }?.also {
            log.info("Extracted rating for book ${book.title} from source ${crawlerSpec.name}: ${it.score}")
        } ?: run {
            log.warn("Failed to extract rating score for book ${book.title} from source ${crawlerSpec.name}")
            null
        }
    }
}
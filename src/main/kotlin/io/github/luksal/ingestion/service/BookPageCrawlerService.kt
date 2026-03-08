package io.github.luksal.ingestion.service

import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.model.RatingSourceUpdate
import io.github.luksal.book.model.RatingUpdate
import io.github.luksal.config.CrawlerSpecification
import io.github.luksal.config.ProxiesProperties
import io.github.luksal.ingestion.crawler.jpa.PageCrawlerJpaRepository
import io.github.luksal.ingestion.crawler.jpa.entity.PageCrawlerConfigEntity
import io.github.luksal.ingestion.crawler.service.PageCrawler
import io.github.luksal.ingestion.fetcher.PageFetcher
import io.github.luksal.ingestion.mappper.IngestionMapper
import io.github.luksal.util.ext.logger
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.net.URLEncoder

@Service
class BookPageCrawlerService(
    private val pageCrawlerRepository: PageCrawlerJpaRepository,
    private val pageFetcher: PageFetcher,
    private val pageCrawler: PageCrawler,
    private val proxiesProperties: ProxiesProperties,
    private val redisTemplate: RedisTemplate<Any, Any>
) {

    private val log = logger()

    companion object {
        private const val FORMATTED_TITLE_PLACEHOLDER = "{formattedTitle}"
    }

    fun crawlBookPage(crawlerId: Long, book: BookSearchResponse): RatingUpdate? {
        return pageCrawlerRepository.findById(crawlerId).orElseThrow()
            .takeIf { it.enabled }
            ?.let { crawler -> getRating(book, crawler) }
    }

    private fun getRating(book: BookSearchResponse, crawler: PageCrawlerConfigEntity): RatingUpdate? {
        val cached = redisTemplate.opsForValue()["rating:${crawler.name}:${book.id}"] as? RatingUpdate

        return cached?.also {
            log.info("Rating for book ${book.title} from source ${crawler.name} found in cache: ${it.score}")
        } ?: crawlAndExtractRating(book, IngestionMapper.map(crawler))?.also {
            redisTemplate.opsForValue()["rating:${crawler.name}:${book.id}"] =  it
        }
    }

    private fun crawlAndExtractRating(
        book: BookSearchResponse,
        crawlerSpec: CrawlerSpecification
    ): RatingUpdate? {
        val searchUrl = composeSearchBookUrl(book, crawlerSpec)
        log.info("Composed search url: $searchUrl")

        return fetch(searchUrl, crawlerSpec)?.let { searchPageHtml ->
            return pageCrawler.extractBookPageUrl(searchPageHtml, crawlerSpec)?.let {
                //val pageUrl = composeBookPageUrl(it, crawlerSpec)
                log.info("Composed page url: $it")
                fetch(it, crawlerSpec).takeIf { bookPage -> bookPage?.isNotEmpty() ?: false }
                    ?.let { bookPage ->
                        extractRating(bookPage, crawlerSpec, book)
                    }
            }
        }
    }

    private fun fetch(url: String, crawlerSpec: CrawlerSpecification): String? {
        //TODO change logic for choosing proxy and invoking given fetch methods
        return if (crawlerSpec.proxyEnabled) {
            val proxy = proxiesProperties.proxies.firstOrNull { it.name == crawlerSpec.proxyName }
                ?: throw IllegalStateException("Proxy ${crawlerSpec.proxyName} not found for crawler ${crawlerSpec.name}")
            if (crawlerSpec.name == "amazon-books") {
                pageFetcher.fetchLocalProxyWitSession(url, proxy)
            } else {
                pageFetcher.fetchLocalProxy(url, proxy)
            }
        } else {
            pageFetcher.fetchNoProxy(url)
        }
    }

    private fun composeSearchBookUrl(book: BookSearchResponse, crawlerSpec: CrawlerSpecification): String {
        val searchTitle = book.title
            .let { URLEncoder.encode(it, Charsets.UTF_8) }
        val searchPath = crawlerSpec.path.search.replace(FORMATTED_TITLE_PLACEHOLDER, searchTitle)
        val searchUrl = "${crawlerSpec.baseUrl}$searchPath"
        return searchUrl
    }

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
            log.info("Extracted rating for book \"${book.title}\" from source ${crawlerSpec.name}: ${it.score}")
        } ?: run {
            log.warn("Failed to extract rating score for book \"${book.title}\" from source ${crawlerSpec.name}")
            null
        }
    }

}
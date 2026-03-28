package io.github.luksal.ingestion.service

import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.model.RatingSourceUpdate
import io.github.luksal.book.model.RatingUpdate
import io.github.luksal.config.ProxiesProperties
import io.github.luksal.exception.SearchPageNotLoadedException
import io.github.luksal.ingestion.crawler.dto.CrawlerConfig
import io.github.luksal.ingestion.crawler.jpa.PageCrawlerJpaRepository
import io.github.luksal.ingestion.crawler.jpa.entity.PageCrawlerConfigEntity
import io.github.luksal.ingestion.crawler.mapper.CrawlerConfigMapper.toConfig
import io.github.luksal.ingestion.crawler.service.PageCrawler
import io.github.luksal.ingestion.fetcher.PageFetcher
import io.github.luksal.util.ext.intersectPercentage
import io.github.luksal.util.ext.logger
import io.github.luksal.util.ext.normalizeStandardChars
import io.github.luksal.util.ext.percentageLevenshteinDistance
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.math.BigDecimal
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
        private const val FORMATTED_SEARCH_PARAMS_PLACEHOLDER = "{formattedSearchParams}"
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
        } ?: crawlAndExtractRating(book, crawler.toConfig())?.also {
            redisTemplate.opsForValue()["rating:${crawler.name}:${book.id}"] = it
        }
    }

    private fun crawlAndExtractRating(
        book: BookSearchResponse,
        crawlerSpec: CrawlerConfig
    ): RatingUpdate? {
        val searchUrl = composeSearchBookUrl(book, crawlerSpec)
        log.info("Composed search url: $searchUrl")

        return fetch(searchUrl, crawlerSpec)?.let { searchPageHtml ->
            if(!pageCrawler.isSearchPageLoaded(searchPageHtml, crawlerSpec)) {
                throw SearchPageNotLoadedException(
                    "Unable to load search page for book ${book.title} from source ${crawlerSpec.name}"
                )
            }
            if (crawlerSpec.path.isRatingAvailableOnSearch) {
                return extractRating(searchPageHtml, crawlerSpec, book)
            }
            return pageCrawler.extractBookPageUrl(searchPageHtml, crawlerSpec)?.let {
                log.info("Composed page url: $it")
                fetch(it, crawlerSpec).takeIf { bookPage -> bookPage?.isNotEmpty() ?: false }
                    ?.let { bookPage ->
                        extractRating(bookPage, crawlerSpec, book)
                    }
            }
        }
    }

    private fun fetch(url: String, crawlerSpec: CrawlerConfig): String? {
        return if (crawlerSpec.proxyEnabled) {
            val proxy = proxiesProperties.proxies.firstOrNull { it.name == crawlerSpec.proxyName }
                ?: throw IllegalStateException("Proxy ${crawlerSpec.proxyName} not found for crawler ${crawlerSpec.name}")
            if (crawlerSpec.proxySessionEnabled) {
                pageFetcher.fetchLocalProxyWitSession(url, crawlerSpec.forwardingProxyEnabled, proxy)
            } else {
                pageFetcher.fetchLocalProxy(url, proxy)
            }
        } else {
            pageFetcher.fetchNoProxy(url)
        }
    }

    private fun composeSearchBookUrl(book: BookSearchResponse, crawlerSpec: CrawlerConfig): String {
        var searchParams = ""
        if (crawlerSpec.path.includeAuthorsForSearch) {
            val searchAuthor = book.authors.firstOrNull()
                ?.normalizeStandardChars()
                ?.let { URLEncoder.encode(it, Charsets.UTF_8) }
                .orEmpty()
            searchParams += searchAuthor
        }
        val searchTitle = book.title
            .let { URLEncoder.encode(it, Charsets.UTF_8) }
        searchParams += "+$searchTitle"
        val searchPath = crawlerSpec.path.search.replace(FORMATTED_SEARCH_PARAMS_PLACEHOLDER, searchParams)
        val searchUrl = "${crawlerSpec.baseUrl}$searchPath"
        return searchUrl
    }

    private fun extractRating(
        bookPage: String,
        crawlerSpec: CrawlerConfig,
        book: BookSearchResponse
    ): RatingUpdate? {
        val ratingScore = pageCrawler.extractRatingScore(bookPage, crawlerSpec)
        val ratingCount = pageCrawler.extractRatingCount(bookPage, crawlerSpec)
        val title = pageCrawler.extractTitle(bookPage, crawlerSpec)
        val authors = pageCrawler.extractAuthors(bookPage, crawlerSpec)
            .map { it.normalizeStandardChars() }.toSet()

        val bookAuthors = book.authors.map { it.normalizeStandardChars() }.toSet()
        return ratingScore?.let {
            RatingUpdate(
                score = it,
                count = ratingCount ?: 0,
                source = RatingSourceUpdate(name = crawlerSpec.name, url = crawlerSpec.baseUrl),
                titleConfidenceIndicator = book.title.percentageLevenshteinDistance(title),
                authorsConfidenceIndicator = authors.intersectPercentage(bookAuthors)
            )
        }?.also {
            redisTemplate.opsForValue()["confidence-indicator:${book.id}"] = ConfidenceIndicatorCheck(
                title = book.title,
                extractedTitle = title,
                authors = book.authors.toSet(),
                extractedAuthors = authors,
                titleConfidenceIndicator = it.titleConfidenceIndicator,
                authorsConfidenceIndicator = it.authorsConfidenceIndicator
            )
            log.info("Extracted rating for book \"${book.title}\" from source ${crawlerSpec.name}: ${it.score}")
        } ?: run {
            log.warn("No rating score for book \"${book.title}\" from source ${crawlerSpec.name}")
            null
        }
    }

    data class ConfidenceIndicatorCheck(
        val title: String,
        val extractedTitle: String,
        val authors: Set<String>,
        val extractedAuthors: Set<String>,
        val titleConfidenceIndicator: BigDecimal,
        val authorsConfidenceIndicator: BigDecimal,
    )
}
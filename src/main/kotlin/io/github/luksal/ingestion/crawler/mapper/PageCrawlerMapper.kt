package io.github.luksal.ingestion.crawler.mapper

import io.github.luksal.ingestion.crawler.api.dto.Crawler
import io.github.luksal.ingestion.crawler.api.dto.CrawlerSearchDetails
import io.github.luksal.ingestion.crawler.api.dto.Path
import io.github.luksal.ingestion.crawler.api.dto.RateLimit
import io.github.luksal.ingestion.crawler.jpa.entity.CrawlerPath
import io.github.luksal.ingestion.crawler.jpa.entity.PageCrawlerConfigEntity

object CrawlerConfigMapper {
    fun PageCrawlerConfigEntity.map(): Crawler =
        Crawler(
            id = id,
            name = name,
            enabled = enabled,
            baseUrl = baseUrl,
            rateLimit = RateLimit(
                requestsPerMinute = rateLimit.requestsPerMinute,
                burst = rateLimit.burst
            ),
            path = Path(
                bookResultSelector = path.bookResultSelector,
                bookRatingCountSelector = path.bookRatingCountSelector,
                bookRatingScoreSelector = path.bookRatingScoreSelector,
                search = path.search,
                includeAuthorsForSearch = path.includeAuthorsForSearch,
                titleSpaceSeparator = path.titleSpaceSeparator,
                bookTitleSelector = path.bookTitleSelector,
                bookAuthorsSelector = path.bookAuthorsSelector,
            ),
            proxyEnabled = proxyEnabled,
            proxyName = proxyName
        )

    fun Crawler.map(id: Long? = null): PageCrawlerConfigEntity =
        PageCrawlerConfigEntity(
            id = id ?: id,
            name = name,
            enabled = enabled,
            baseUrl = baseUrl,
            rateLimit = io.github.luksal.ingestion.crawler.jpa.entity.RateLimit(
                requestsPerMinute = rateLimit.requestsPerMinute,
                burst = rateLimit.burst
            ),
            path = CrawlerPath(
                bookResultSelector = path.bookResultSelector,
                bookRatingCountSelector = path.bookRatingCountSelector,
                bookRatingScoreSelector = path.bookRatingScoreSelector,
                search = path.search,
                includeAuthorsForSearch = path.includeAuthorsForSearch,
                titleSpaceSeparator = path.titleSpaceSeparator,
                bookTitleSelector = path.bookTitleSelector,
                bookAuthorsSelector = path.bookAuthorsSelector,
            ),
            proxyEnabled = proxyEnabled,
            proxyName = proxyName
        )

    fun mapToSearchResponse(entity: Crawler): CrawlerSearchDetails =
        CrawlerSearchDetails(
            id = entity.id!!,
            name = entity.name,
            enabled = entity.enabled,
            baseUrl = entity.baseUrl
        )
}
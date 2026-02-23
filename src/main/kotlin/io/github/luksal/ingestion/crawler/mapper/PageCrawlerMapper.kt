package io.github.luksal.ingestion.crawler.mapper

import io.github.luksal.ingestion.crawler.api.dto.Crawler
import io.github.luksal.ingestion.crawler.api.dto.Path
import io.github.luksal.ingestion.crawler.api.dto.RateLimit
import io.github.luksal.ingestion.crawler.jpa.entity.CrawlerPath
import io.github.luksal.ingestion.crawler.jpa.entity.PageCrawlerConfigEntity

object CrawlerConfigMapper {
    fun map(entity: PageCrawlerConfigEntity): Crawler =
        Crawler(
            id = entity.id,
            enabled = entity.enabled,
            baseUrl = entity.baseUrl,
            rateLimit = RateLimit(
                requestsPerMinute = entity.rateLimit.requestsPerMinute,
                burst = entity.rateLimit.burst
            ),
            path = Path(
                bookResultSelector = entity.path.bookResultSelector,
                bookRatingCountSelector = entity.path.bookRatingCountSelector,
                bookRatingScoreSelector = entity.path.bookRatingScoreSelector,
                search = entity.path.search,
                titleSpaceSeparator = entity.path.titleSpaceSeparator
            ),
            proxyEnabled = entity.proxyEnabled
        )

    fun map(id: Long? = null, domain: Crawler): PageCrawlerConfigEntity =
        PageCrawlerConfigEntity(
            id = id ?: domain.id,
            enabled = domain.enabled,
            baseUrl = domain.baseUrl,
            rateLimit = io.github.luksal.ingestion.crawler.jpa.entity.RateLimit(
                requestsPerMinute = domain.rateLimit.requestsPerMinute,
                burst = domain.rateLimit.burst
            ),
            path = CrawlerPath(
                bookResultSelector = domain.path.bookResultSelector,
                bookRatingCountSelector = domain.path.bookRatingCountSelector,
                bookRatingScoreSelector = domain.path.bookRatingScoreSelector,
                search = domain.path.search,
                titleSpaceSeparator = domain.path.titleSpaceSeparator
            ),
            proxyEnabled = domain.proxyEnabled
        )
}
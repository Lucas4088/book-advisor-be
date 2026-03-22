package io.github.luksal.ingestion.crawler.mapper

import io.github.luksal.ingestion.crawler.dto.CrawlerConfig
import io.github.luksal.ingestion.crawler.dto.CrawlerSearchDetails
import io.github.luksal.ingestion.crawler.dto.Path
import io.github.luksal.ingestion.crawler.dto.RateLimit
import io.github.luksal.ingestion.crawler.jpa.entity.CrawlerPath
import io.github.luksal.ingestion.crawler.jpa.entity.PageCrawlerConfigEntity

object CrawlerConfigMapper {
    fun PageCrawlerConfigEntity.toEntity(): CrawlerConfig =
        CrawlerConfig(
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
                isRatingAvailableOnSearch = path.isRatingAvailableOnSearch,
                bookFirstElementSearchSelector = path.bookFirstElementSearchSelector,
                bookRatingScoreSearchSelector = path.bookRatingScoreSearchSelector,
                bookRatingCountSearchSelector = path.bookRatingCountSearchSelector,
                bookTitleSearchSelector = path.bookTitleSearchSelector,
                bookAuthorsSearchSelector = path.bookAuthorsSearchSelector,
            ),
            proxyEnabled = proxyEnabled,
            proxyName = proxyName,
            proxySessionEnabled = proxySessionEnabled,
            forwardingProxyEnabled = forwardingProxyEnabled
        )

    fun CrawlerConfig.toEntity(id: Long? = null): PageCrawlerConfigEntity =
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
                isRatingAvailableOnSearch = path.isRatingAvailableOnSearch,
                bookFirstElementSearchSelector = path.bookFirstElementSearchSelector,
                bookRatingScoreSearchSelector = path.bookRatingScoreSearchSelector,
                bookRatingCountSearchSelector = path.bookRatingCountSearchSelector,
                bookTitleSearchSelector = path.bookTitleSearchSelector,
                bookAuthorsSearchSelector = path.bookAuthorsSearchSelector,
            ),
            proxyEnabled = proxyEnabled,
            proxyName = proxyName,
            proxySessionEnabled = proxySessionEnabled,
            forwardingProxyEnabled = forwardingProxyEnabled,
        )

    fun CrawlerConfig.toSearchResponse(): CrawlerSearchDetails =
        CrawlerSearchDetails(
            id = id!!,
            name = name,
            enabled = enabled,
            baseUrl = baseUrl
        )
}
package io.github.luksal.ingestion.mapper

import io.github.luksal.book.db.jpa.model.event.ScheduledBookBasicInfoPopulationEventEntity
import io.github.luksal.commons.dto.EventMeta
import io.github.luksal.ingestion.crawler.dto.CrawlerConfig
import io.github.luksal.ingestion.crawler.dto.Path
import io.github.luksal.ingestion.crawler.dto.RateLimit
import io.github.luksal.ingestion.api.dto.ScheduledBookBasicInfoPopulationEvent
import io.github.luksal.ingestion.crawler.jpa.entity.PageCrawlerConfigEntity


object IngestionMapper {

    fun map(source: ScheduledBookBasicInfoPopulationEventEntity): ScheduledBookBasicInfoPopulationEvent {
        return ScheduledBookBasicInfoPopulationEvent(
            id = source.id!!,
            year = source.year,
            lang = source.lang,
            meta = map(source.meta)
        )
    }

    fun map(eventMeta: io.github.luksal.commons.jpa.EventMeta): EventMeta {
        return EventMeta(
            status = eventMeta.status,
            errorMessage = eventMeta.errorMessage,
            createdAt = eventMeta.createdAt,
            updatedAt = eventMeta.updatedAt,
        )
    }

    fun PageCrawlerConfigEntity.map(): CrawlerConfig =
        CrawlerConfig(
            id = id,
            name = name,
            baseUrl = baseUrl,
            enabled = enabled,
            path = Path(
                bookResultSelector = path.bookResultSelector,
                bookRatingScoreSelector = path.bookRatingScoreSelector,
                bookRatingCountSelector = path.bookRatingCountSelector,
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
            rateLimit = RateLimit(
                requestsPerMinute = rateLimit.requestsPerMinute,
                burst = rateLimit.burst
            ),
            proxyEnabled = proxyEnabled,
            proxyName = proxyName,
            proxySessionEnabled = proxySessionEnabled,
            forwardingProxyEnabled = forwardingProxyEnabled
        )


}
package io.github.luksal.ingestion.mappper

import io.github.luksal.book.db.jpa.model.event.ScheduledBookBasicInfoPopulationEventEntity
import io.github.luksal.book.model.Rating
import io.github.luksal.book.model.RatingSourceUpdate
import io.github.luksal.book.model.RatingUpdate
import io.github.luksal.commons.dto.EventMeta
import io.github.luksal.config.CrawlerSpecification
import io.github.luksal.config.Path
import io.github.luksal.config.RateLimit
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

    fun map(entity: PageCrawlerConfigEntity): CrawlerSpecification =
        CrawlerSpecification(
            name = entity.name,
            baseUrl = entity.baseUrl,
            enabled = true,
            path = Path(
                bookResultSelector = entity.path.bookResultSelector,
                bookRatingScoreSelector = entity.path.bookRatingScoreSelector,
                bookRatingCountSelector = entity.path.bookRatingCountSelector,
                search = entity.path.search,
                titleSpaceSeparator = entity.path.titleSpaceSeparator
            ),
            rateLimit = RateLimit(
                requestsPerMinute = entity.rateLimit.requestsPerMinute,
                burst = entity.rateLimit.burst
            ),
            proxyEnabled = entity.proxyEnabled
        )


}
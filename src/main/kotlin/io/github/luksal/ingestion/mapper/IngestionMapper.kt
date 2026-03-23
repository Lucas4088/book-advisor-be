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

}
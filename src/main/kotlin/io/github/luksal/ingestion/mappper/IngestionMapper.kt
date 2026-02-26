package io.github.luksal.ingestion.mappper

import io.github.luksal.book.db.jpa.model.event.ScheduledBookBasicInfoPopulationEventEntity
import io.github.luksal.commons.dto.EventMeta
import io.github.luksal.ingestion.api.dto.ScheduledBookBasicInfoPopulationEvent


object IngestionMapper {

    fun map(source: ScheduledBookBasicInfoPopulationEventEntity): ScheduledBookBasicInfoPopulationEvent {
        return ScheduledBookBasicInfoPopulationEvent(
            id = source.id!!,
            year = source.year,
            lang = source.lang,
            meta = map(source.meta)
        )
    }

    fun map(eventMeta: io.github.luksal.book.common.jpa.event.EventMeta): EventMeta {
        return EventMeta(
            status = eventMeta.status,
            errorMessage = eventMeta.errorMessage,
            createdAt = eventMeta.createdAt,
            updatedAt = eventMeta.updatedAt,
        )
    }

}
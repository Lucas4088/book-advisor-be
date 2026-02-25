package io.github.luksal.ingestion.api.dto

import io.github.luksal.book.common.jpa.event.EventStatus
import io.github.luksal.commons.dto.EventMeta

data class ScheduleBookBasicInfoRequest(
    val fromYear: Int,
    val toYear: Int,
    val lang: String
)

data class ScheduledBookBasicInfoSearchRequest(
    val fromYear: Int?,
    val toYear: Int?,
    val lang: String?,
    val status: List<EventStatus>?
)

data class ScheduledBookBasicInfoPopulationEvent(
    val id: Long,
    val year: Int,
    val lang: String,
    var meta: EventMeta,
)

package io.github.luksal.ingestion.api.dto

import io.github.luksal.commons.dto.EventMeta


data class ScheduledBookBasicInfoPopulationEvent(
    val id: Long,
    val year: Int,
    val lang: String,
    var meta: EventMeta,
)
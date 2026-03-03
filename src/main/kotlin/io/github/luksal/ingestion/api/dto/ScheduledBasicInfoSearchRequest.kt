package io.github.luksal.ingestion.api.dto

import io.github.luksal.commons.dto.EventStatus

data class ScheduledBasicInfoSearchRequest(
    val fromYear: Int?,
    val toYear: Int?,
    val lang: String?,
    val status: List<EventStatus>?
)

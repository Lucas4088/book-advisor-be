package io.github.luksal.ingestion.api.dto

import io.github.luksal.book.common.jpa.event.EventStatus

data class ScheduledBasicInfoSearchRequest(
    val fromYear: Int?,
    val toYear: Int?,
    val lang: String?,
    val status: List<EventStatus>?
)

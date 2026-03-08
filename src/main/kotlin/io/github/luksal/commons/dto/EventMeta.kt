package io.github.luksal.commons.dto

import java.time.Instant

data class EventMeta(
    var status: EventStatus,

    var errorMessage: String?,

    val createdAt: Instant,

    var updatedAt: Instant?,
)
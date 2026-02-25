package io.github.luksal.commons.dto

import io.github.luksal.book.common.jpa.event.EventStatus

data class EventMeta(
    var status: EventStatus,

    var errorMessage: String?,

    val createdAt: Long,

    var updatedAt: Long?,
)
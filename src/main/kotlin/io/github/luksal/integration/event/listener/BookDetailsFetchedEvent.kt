package io.github.luksal.integration.event.listener

import io.github.luksal.commons.dto.EventStatus
import io.github.luksal.event.Event

data class BookDetailsFetchedEvent(
    val sourceName: String,
    val status: EventStatus
) : Event

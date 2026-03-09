package io.github.luksal.commons.jpa

import io.github.luksal.commons.dto.EventStatus
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.time.Instant

@Embeddable
class EventMeta(
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: EventStatus = EventStatus.PENDING,

    @Column
    var errorMessage: String? = null,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column
    var updatedAt: Instant? = null,
) {
    //TODO should be mainly handled by events itself, introduce base event interface or abstract class to enforce this contract
    fun markAsPending() {
        status = EventStatus.PENDING
        updatedAt = Instant.now()
    }

    fun markAsInProgress() {
        status = EventStatus.IN_PROGRESS
        updatedAt = Instant.now()
    }

    fun markAsSuccess() {
        status = EventStatus.SUCCESS
        updatedAt = Instant.now()
    }

    fun markAsSkipped() {
        status = EventStatus.SKIPPED
        updatedAt = Instant.now()
    }

    fun markAsFailed(errorMessage: String) {
        status = EventStatus.ERROR
        this.errorMessage = errorMessage
        updatedAt = Instant.now()
    }

    fun markAsUnprocessable() {
        status = EventStatus.UNPROCESSABLE
        updatedAt = Instant.now()
    }
}
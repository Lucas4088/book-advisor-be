package io.github.luksal.book.common.jpa.event

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
class EventMeta(
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: EventStatus = EventStatus.PENDING,

    @Column
    var errorMessage: String? = null,

    @Column(nullable = false)
    val createdAt: Long = System.currentTimeMillis(),

    @Column
    var updatedAt: Long? = null,
) {
    fun markAsSuccess() {
        status = EventStatus.SUCCESS
        updatedAt = System.currentTimeMillis()
    }

    fun markAsSkipped() {
        status = EventStatus.SKIPPED
        updatedAt = System.currentTimeMillis()
    }

    fun markAsFailed(errorMessage: String) {
        status = EventStatus.ERROR
        this.errorMessage = errorMessage
        updatedAt = System.currentTimeMillis()
    }
}
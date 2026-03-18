package io.github.luksal.integration.db

import io.github.luksal.commons.jpa.EventMeta
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "book_details_fetched_events")
data class BookDetailsFetchedEventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val sourceName: String,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "status", column = Column(name = "status", nullable = false)),
        AttributeOverride(name = "errorMessage", column = Column(name = "error_message")),
        AttributeOverride(name = "timestamp", column = Column(name = "timestamp", nullable = false)),
    )
    var meta: EventMeta = EventMeta(),
)

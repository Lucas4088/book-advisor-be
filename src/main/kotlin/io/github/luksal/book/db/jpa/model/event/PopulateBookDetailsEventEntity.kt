package io.github.luksal.book.db.jpa.model.event

import io.github.luksal.book.common.jpa.event.EventMeta
import jakarta.persistence.*

@Entity
@Table(name = "populate_book_details_events")
data class PopulateBookDetailsEventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    val bookId: String,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "status", column = Column(name = "status", nullable = false)),
        AttributeOverride(name = "errorMessage", column = Column(name = "error_message")),
        AttributeOverride(name = "timestamp", column = Column(name = "timestamp", nullable = false)),
    )
    var meta: EventMeta = EventMeta(),
)
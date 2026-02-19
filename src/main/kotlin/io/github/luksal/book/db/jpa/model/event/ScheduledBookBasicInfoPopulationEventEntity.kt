package io.github.luksal.book.db.jpa.model.event

import io.github.luksal.book.common.jpa.event.EventMeta
import jakarta.persistence.*

@Entity
@Table(name = "book_basic_data_population_events")
class ScheduledBookBasicInfoPopulationEventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val year: Int,

    @Column(nullable = false)
    val lang: String,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "status", column = Column(name = "status", nullable = false)),
        AttributeOverride(name = "errorMessage", column = Column(name = "error_message")),
        AttributeOverride(name = "timestamp", column = Column(name = "timestamp", nullable = false)),
    )
    var meta: EventMeta = EventMeta(),
)
package io.github.luksal.book.db.jpa.model.event

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "sync_books_events")
class SyncBookEventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    val bookId: String,

    @Column(nullable = false)
    var processed: Boolean = false,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: EventStatus = EventStatus.NEW,

    @Column(nullable = true)
    var errorMessage: String? = null,

    @Column(nullable = false)
    var timestamp: Long,
)
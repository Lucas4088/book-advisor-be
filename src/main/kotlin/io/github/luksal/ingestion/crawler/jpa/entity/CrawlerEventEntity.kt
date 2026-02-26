package io.github.luksal.ingestion.crawler.jpa.entity

import io.github.luksal.book.common.jpa.event.EventMeta
import io.github.luksal.ingestion.crawler.api.dto.Crawler
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table("scheduled_book_crawler_events")
class ScheduledBookCrawlerEventEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val bookId: String,

    @Column(nullable = false)
    val crawlerId: Long,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "status", column = Column(name = "status", nullable = false)),
        AttributeOverride(name = "errorMessage", column = Column(name = "error_message")),
        AttributeOverride(name = "createdAt", column = Column(name = "created_at", nullable = false)),
        AttributeOverride(name = "updatedAt", column = Column(name = "updated_at", nullable = false)),
    )
    val meta: EventMeta = EventMeta(),
)
package io.github.luksal.ingestion.crawler.jpa.entity

import io.github.luksal.commons.dto.RetryableEvent
import io.github.luksal.commons.jpa.EventMeta
import io.github.luksal.commons.jpa.RetryMeta
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "scheduled_book_crawler_events")
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

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "retryCount", column = Column(name = "retry_count", nullable = false)),
        AttributeOverride(name = "lastRetryAt", column = Column(name = "last_retry_at")),
        AttributeOverride(name = "nextRetryAt", column = Column(name = "next_retry_at"))
    )
    val retry: RetryMeta = RetryMeta()
) : RetryableEvent {
    override fun getRetryCount(): Int = retry.count

    override fun incrementRetryCount(): Int = retry.increment()

    override fun resetStatus() = meta.markAsPending()

    override fun setNextRetryAt(nextRetryAt: Instant) {
        retry.nextRetryAt = nextRetryAt
    }

    fun markAsFailed(errorMessage: String) =
        if (getRetryCount() >= 4) {
            meta.markAsUnprocessable()
        } else {
            meta.markAsFailed(errorMessage)
        }
}
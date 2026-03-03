package io.github.luksal.ingestion.crawler.jpa

import io.github.luksal.ingestion.crawler.jpa.entity.ScheduledBookCrawlerEventEntity
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ScheduledBookCrawlerEventRepository : JpaRepository<ScheduledBookCrawlerEventEntity, Long> {

/*    @Query(
        """
                 WITH cte AS (
                 SELECT id FROM scheduled_book_crawler_events WHERE status = 'PENDING'
                 ORDER BY created_at LIMIT :limit FOR UPDATE SKIP LOCKED )
                 UPDATE scheduled_book_crawler_events e SET status = 'IN_PROGRESS',
                 updated_at = NOW()
                 FROM cte WHERE e.id = cte.id RETURNING e.*
                 """,
        nativeQuery = true
    )
    fun claimPending(@Param("limit") limit: Int): List<ScheduledBookCrawlerEventEntity>?*/


    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
                 select e from ScheduledBookCrawlerEventEntity e where e.meta.status = 'PENDING'
                 and e.crawlerId = :crawlerId
                 order by e.meta.createdAt
                 limit :limit
                 """
    )
    fun claimPending(@Param("limit") limit: Int, @Param("crawlerId") crawlerId: Long): List<ScheduledBookCrawlerEventEntity>?
}
package io.github.luksal.ingestion.crawler.jpa

import io.github.luksal.commons.dto.EventStatus
import io.github.luksal.ingestion.crawler.jpa.entity.ScheduledBookCrawlerOnDemandEventEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ScheduledBookCrawlerOnDemandEventRepository : JpaRepository<ScheduledBookCrawlerOnDemandEventEntity, Long> {
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
                 select e from ScheduledBookCrawlerOnDemandEventEntity e where e.meta.status = :status
                 and e.crawlerId = :crawlerId
                 order by e.meta.createdAt asc
                 limit :limit
                 """
    )
    fun claimByStatus(
        @Param("status") status: EventStatus,
        @Param("limit") limit: Int,
        @Param("crawlerId") crawlerId: Long
    ): List<ScheduledBookCrawlerOnDemandEventEntity>?


    @Query(
        """
                 select e from ScheduledBookCrawlerOnDemandEventEntity e
                 where e.bookId = :bookId
                 and e.crawlerId = :crawlerId
                 """
    )
    fun findByBookIdAndCrawlerId(
        @Param("bookId") bookId: String,
        @Param("crawlerId") crawlerId: Long
    ) : ScheduledBookCrawlerOnDemandEventEntity?

}
package io.github.luksal.ingestion.crawler.jpa

import io.github.luksal.ingestion.crawler.jpa.entity.ScheduledBookCrawlerEventEntity
import org.springframework.data.domain.Page
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ScheduledBookCrawlerEventJpa : JpaRepository<ScheduledBookCrawlerEventEntity, Long> {

    @Query("SELECT e FROM ScheduledBookCrawlerEventEntity e WHERE e.meta.status = 'PENDING' ORDER BY e.meta.createdAt ASC")
    fun findAllPending(): Page<ScheduledBookCrawlerEventEntity>?
}
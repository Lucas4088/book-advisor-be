package io.github.luksal.ingestion.crawler.jpa

import io.github.luksal.ingestion.crawler.jpa.entity.PageCrawlerConfigEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PageCrawlerJpaRepository : JpaRepository<PageCrawlerConfigEntity, Long> {
}
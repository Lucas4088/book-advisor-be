package io.github.luksal.ingestion.crawler.service

import io.github.luksal.ingestion.crawler.api.dto.Crawler
import io.github.luksal.ingestion.crawler.jpa.PageCrawlerJpaRepository
import io.github.luksal.ingestion.crawler.jpa.ScheduledBookCrawlerEventJpa
import io.github.luksal.ingestion.crawler.jpa.entity.ScheduledBookCrawlerEventEntity
import io.github.luksal.ingestion.crawler.mapper.CrawlerConfigMapper
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PageCrawlerCrudService(
    val pageCrawlerJpaRepository: PageCrawlerJpaRepository
) {

    @CacheEvict(cacheNames = ["crawlers"])
    fun create(request: Crawler) =
        pageCrawlerJpaRepository.save(CrawlerConfigMapper.map(domain = request))

    fun getById(id: Long): Crawler? =
        pageCrawlerJpaRepository.findByIdOrNull(id)
            ?.let { CrawlerConfigMapper.map(it) }

    @Cacheable(cacheNames = ["crawlers"])
    fun findAll(): List<Crawler> =
        pageCrawlerJpaRepository.findAll().map { CrawlerConfigMapper.map(it) }

    @CacheEvict(cacheNames = ["crawlers"], key = "#id")
    fun delete(id: Long) =
        pageCrawlerJpaRepository.deleteById(id)

    @CacheEvict(cacheNames = ["crawlers"], key = "#id")
    fun update(id: Long, request: Crawler) {
        pageCrawlerJpaRepository.findById(id)
            .orElseThrow()

        CrawlerConfigMapper.map(id,request)
            .let { pageCrawlerJpaRepository.save(it) }
    }

/*    fun findAllScheduledEventsByStatus(status: String) =
        crawlerEventJpa.findAllByMeta_Status(status, org.springframework.data.domain.PageRequest.of(0, 100))
            ?.content
            ?.map { io.github.luksal.ingestion.crawler.mapper.CrawlerEventMapper.map(it) }
             ?: emptyList()*/
}
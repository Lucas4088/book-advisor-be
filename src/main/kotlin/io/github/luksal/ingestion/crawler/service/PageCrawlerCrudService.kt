package io.github.luksal.ingestion.crawler.service

import io.github.luksal.ingestion.crawler.api.dto.Crawler
import io.github.luksal.ingestion.crawler.jpa.PageCrawlerJpaRepository
import io.github.luksal.ingestion.crawler.mapper.CrawlerConfigMapper
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PageCrawlerCrudService(
    val pageCrawlerJpaRepository: PageCrawlerJpaRepository
) {

    @CacheEvict(cacheNames = ["crawlers"], allEntries = true, cacheManager = "caffeineCacheManager")
    fun create(request: Crawler) =
        pageCrawlerJpaRepository.save(CrawlerConfigMapper.map(domain = request))

    fun getById(id: Long): Crawler? =
        pageCrawlerJpaRepository.findByIdOrNull(id)
            ?.let { CrawlerConfigMapper.map(it) }

    @Cacheable(cacheNames = ["crawlers"], cacheManager = "caffeineCacheManager")
    fun findAll(): List<Crawler> =
        pageCrawlerJpaRepository.findAll().map { CrawlerConfigMapper.map(it) }

    @CacheEvict(cacheNames = ["crawlers"], allEntries = true, cacheManager = "caffeineCacheManager")
    fun delete(id: Long) =
        pageCrawlerJpaRepository.deleteById(id)

    @CacheEvict(cacheNames = ["crawlers"], allEntries = true, cacheManager = "caffeineCacheManager")
    fun update(id: Long, request: Crawler) {
        pageCrawlerJpaRepository.findById(id)
            .orElseThrow()

        CrawlerConfigMapper.map(id, request)
            .let { pageCrawlerJpaRepository.save(it) }
    }
}
package io.github.luksal.ingestion.crawler.service

import io.github.luksal.ingestion.crawler.dto.CrawlerConfig
import io.github.luksal.ingestion.crawler.jpa.PageCrawlerJpaRepository
import io.github.luksal.ingestion.crawler.mapper.CrawlerConfigMapper.toConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PageCrawlerCrudService(
    val pageCrawlerJpaRepository: PageCrawlerJpaRepository
) {

    @CacheEvict(cacheNames = ["crawlers"], allEntries = true, cacheManager = "caffeineCacheManager")
    fun create(request: CrawlerConfig) =
        pageCrawlerJpaRepository.save(request.toConfig())

    fun getById(id: Long): CrawlerConfig? =
        pageCrawlerJpaRepository.findByIdOrNull(id)?.toConfig()

    @Cacheable(cacheNames = ["crawlers"], cacheManager = "caffeineCacheManager")
    fun findAll(): List<CrawlerConfig> =
        pageCrawlerJpaRepository.findAll().map { it.toConfig() }

    @CacheEvict(cacheNames = ["crawlers"], allEntries = true, cacheManager = "caffeineCacheManager")
    fun delete(id: Long) =
        pageCrawlerJpaRepository.deleteById(id)

    @CacheEvict(cacheNames = ["crawlers"], allEntries = true, cacheManager = "caffeineCacheManager")
    @Transactional
    fun update(id: Long, request: CrawlerConfig) {
        pageCrawlerJpaRepository.findById(id)
            .orElseThrow()
        request.toConfig(id)
            .let { pageCrawlerJpaRepository.save(it) }
    }
}
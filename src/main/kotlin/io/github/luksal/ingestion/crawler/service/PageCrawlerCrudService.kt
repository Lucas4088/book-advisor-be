package io.github.luksal.ingestion.crawler.service

import io.github.luksal.ingestion.crawler.api.dto.Crawler
import io.github.luksal.ingestion.crawler.jpa.PageCrawlerJpaRepository
import io.github.luksal.ingestion.crawler.mapper.CrawlerConfigMapper.map
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
    fun create(request: Crawler) =
        pageCrawlerJpaRepository.save(request.map())

    fun getById(id: Long): Crawler? =
        pageCrawlerJpaRepository.findByIdOrNull(id)?.map()

    @Cacheable(cacheNames = ["crawlers"], cacheManager = "caffeineCacheManager")
    fun findAll(): List<Crawler> =
        pageCrawlerJpaRepository.findAll().map { it.map() }

    @CacheEvict(cacheNames = ["crawlers"], allEntries = true, cacheManager = "caffeineCacheManager")
    fun delete(id: Long) =
        pageCrawlerJpaRepository.deleteById(id)

    @CacheEvict(cacheNames = ["crawlers"], allEntries = true, cacheManager = "caffeineCacheManager")
    @Transactional
    fun update(id: Long, request: Crawler) {
        pageCrawlerJpaRepository.findById(id)
            .orElseThrow()
        request.map(id)
            .let { pageCrawlerJpaRepository.save(it) }
    }
}
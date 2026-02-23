package io.github.luksal.ingestion.crawler.service

import io.github.luksal.ingestion.crawler.api.dto.Crawler
import io.github.luksal.ingestion.crawler.jpa.PageCrawlerJpaRepository
import io.github.luksal.ingestion.crawler.mapper.CrawlerConfigMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PageCrawlerCrudService(val pageCrawlerJpaRepository: PageCrawlerJpaRepository) {

    fun create(request: Crawler) =
        pageCrawlerJpaRepository.save(CrawlerConfigMapper.map(request))

    fun getById(id: Long): Crawler? =
        pageCrawlerJpaRepository.findByIdOrNull(id)
            ?.let { CrawlerConfigMapper.map(it) }

    fun findAll(): List<Crawler> =
        pageCrawlerJpaRepository.findAll().map { CrawlerConfigMapper.map(it) }
}
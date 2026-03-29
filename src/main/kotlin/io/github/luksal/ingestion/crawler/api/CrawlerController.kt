package io.github.luksal.ingestion.crawler.api

import io.github.luksal.ingestion.crawler.dto.CrawlerConfig
import io.github.luksal.ingestion.crawler.dto.CrawlerSearchDetails
import io.github.luksal.ingestion.crawler.mapper.CrawlerConfigMapper.toSearchResponse
import io.github.luksal.ingestion.crawler.service.PageCrawlerCrudService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/crawler")
class CrawlerController(
    val crawlerCrudService: PageCrawlerCrudService
) {

    @PostMapping
    //TODO validation + sanitize input
    fun create(@RequestBody request: CrawlerConfig) =
        crawlerCrudService.create(request)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): CrawlerConfig? =
        crawlerCrudService.getById(id)

    @GetMapping
    fun findAll(): List<CrawlerSearchDetails> =
        crawlerCrudService.findAll().map { it.toSearchResponse() }.sortedBy { it.id }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) =
        crawlerCrudService.delete(id)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: CrawlerConfig) =
        crawlerCrudService.update(id, request)

}
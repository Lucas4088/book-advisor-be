package io.github.luksal.ingestion.crawler.api

import io.github.luksal.ingestion.crawler.api.dto.Crawler
import io.github.luksal.ingestion.crawler.api.dto.CrawlerSearchDetails
import io.github.luksal.ingestion.crawler.mapper.CrawlerConfigMapper
import io.github.luksal.ingestion.crawler.service.PageCrawlerCrudService
import io.github.luksal.ingestion.service.BookIngestionService
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/crawler")
@RestController
class CrawlerController(
    val bookIngestionService: BookIngestionService,
    val crawlerCrudService: PageCrawlerCrudService
) {

    @PostMapping("/crawl/{crawlerId}")
    fun crawl(@PathVariable("crawlerId") crawlerId: Long): String {
        bookIngestionService.crawlForRating(crawlerId)
        return "Crawling finished"
    }

    @PostMapping
    //TODO validation + sanitize input
    fun create(@RequestBody request: Crawler) =
        crawlerCrudService.create(request)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Crawler? =
        crawlerCrudService.getById(id)

    @GetMapping
    fun findAll(): List<CrawlerSearchDetails> =
        crawlerCrudService.findAll().map { CrawlerConfigMapper.mapToSearchResponse(it) }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) =
        crawlerCrudService.delete(id)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: Crawler) =
        crawlerCrudService.update(id, request)

}
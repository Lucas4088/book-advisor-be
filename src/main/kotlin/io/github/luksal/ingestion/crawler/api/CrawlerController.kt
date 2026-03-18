package io.github.luksal.ingestion.crawler.api

import io.github.luksal.commons.dto.EventStatus
import io.github.luksal.ingestion.crawler.api.dto.Crawler
import io.github.luksal.ingestion.crawler.api.dto.CrawlerSearchDetails
import io.github.luksal.ingestion.crawler.mapper.CrawlerConfigMapper
import io.github.luksal.ingestion.crawler.service.PageCrawlerCrudService
import io.github.luksal.ingestion.service.BookRatingIngestionService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/crawler")
class CrawlerController(
    val bookRatingIngestionService: BookRatingIngestionService,
    val crawlerCrudService: PageCrawlerCrudService
) {

    @PostMapping("/crawl/{crawlerId}")
    fun crawl(@PathVariable("crawlerId") crawlerId: Long): String {
        bookRatingIngestionService.crawlForRating(EventStatus.PENDING, crawlerId)
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
        crawlerCrudService.findAll().map { CrawlerConfigMapper.mapToSearchResponse(it) }.sortedBy { it.id }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) =
        crawlerCrudService.delete(id)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: Crawler) =
        crawlerCrudService.update(id, request)

}
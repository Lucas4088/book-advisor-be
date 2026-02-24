package io.github.luksal.ingestion.crawler.api

import io.github.luksal.ingestion.crawler.api.dto.Crawler
import io.github.luksal.ingestion.crawler.api.dto.CrawlerSearchDetails
import io.github.luksal.ingestion.crawler.mapper.CrawlerConfigMapper
import io.github.luksal.ingestion.crawler.service.PageCrawlerCrudService
import io.github.luksal.ingestion.service.BookIngestionService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/crawler")
@RestController
class CrawlerController(
    val bookIngestionService: BookIngestionService,
    val crawlerCrudService: PageCrawlerCrudService
) {

    @PostMapping("/crawl")
    fun crawl(): String {
        bookIngestionService.crawlAndIngest()
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
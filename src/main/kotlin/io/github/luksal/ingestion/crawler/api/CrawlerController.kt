package io.github.luksal.ingestion.crawler.api

import io.github.luksal.ingestion.crawler.api.dto.Crawler
import io.github.luksal.ingestion.crawler.service.PageCrawlerCrudService
import io.github.luksal.ingestion.service.BookIngestionService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
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
    fun create(request: Crawler) =
        crawlerCrudService.create(request)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Crawler? =
        crawlerCrudService.getById(id)

    @GetMapping
    fun findAll(): List<Crawler> =
        crawlerCrudService.findAll()

}
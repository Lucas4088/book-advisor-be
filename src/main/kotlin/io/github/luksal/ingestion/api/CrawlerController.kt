package io.github.luksal.ingestion.api

import io.github.luksal.ingestion.service.BookIngestionService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/crawler")
@RestController
class CrawlerController(val bookIngestionService: BookIngestionService) {


    @PostMapping("/crawl")
    fun crawl(): String {
        bookIngestionService.crawlAndIngest()
        return "Crawling finished"
    }
}
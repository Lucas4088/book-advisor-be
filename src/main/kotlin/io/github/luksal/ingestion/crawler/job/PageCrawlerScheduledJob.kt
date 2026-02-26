package io.github.luksal.ingestion.crawler.job

import io.github.luksal.ingestion.crawler.jpa.ScheduledBookCrawlerEventJpa
import io.github.luksal.ingestion.crawler.service.PageCrawler
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class PageCrawlerScheduledJob(
    private val crawlerEventJpa: ScheduledBookCrawlerEventJpa
    private val crawler: PageCrawler
) {

    @Scheduled(fixedDelay = 1000)
    fun run() {
        crawlerEventJpa.findAllPending()?.let {
            crawler.
        }
    }
}
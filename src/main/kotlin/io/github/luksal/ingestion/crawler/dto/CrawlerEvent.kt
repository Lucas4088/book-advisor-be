package io.github.luksal.ingestion.crawler.dto

import io.github.luksal.commons.dto.EventMeta

data class ScheduledBookCrawlerEvent(
    val id: Long? = null,
    val bookId: String,
    val crawler: CrawlerConfig,
    val meta: EventMeta
)
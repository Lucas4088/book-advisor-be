package io.github.luksal.ingestion.crawler.dto

import io.github.luksal.commons.dto.EventMeta
import io.github.luksal.ingestion.crawler.api.dto.Crawler

data class ScheduledBookCrawlerEvent(
    val id: Long? = null,
    val bookId: String,
    val crawler: Crawler,
    val meta: EventMeta
)
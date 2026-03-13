package io.github.luksal.ingestion.crawler.dto

import io.github.luksal.commons.dto.EventStatus

data class CrawlerEventCountByEventStatus(
    val eventStatus: EventStatus,
    val crawlerId: Long,
    val count: Long
)

data class CrawlerEventCountByCrawlerId(
    val crawlerId: Long,
    val count: Long
)
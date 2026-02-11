package io.github.luksal.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val crawlers: List<CrawlerSpecification>
)

data class CrawlerSpecification(
    val name: String,
    val enabled: Boolean,
    val baseUrl: String,
    val rateLimit: RateLimit,
    val headers: Map<String, String> = emptyMap(),
    val path: Path
)

data class RateLimit(
    val requestsPerMinute: Int,
    val burst: Int
)

data class Path(
    val bookResultSelector: String,
    val search: String,
    val titleSpaceSeparator: String
)
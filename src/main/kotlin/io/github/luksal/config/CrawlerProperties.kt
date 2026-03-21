package io.github.luksal.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class CrawlerProperties(
    val crawlers: List<CrawlerSpecification>
)

data class CrawlerSpecification(
    val name: String,
    val enabled: Boolean,
    val baseUrl: String,
    val rateLimit: RateLimit,
    val headers: Map<String, String> = emptyMap(),
    val path: Path,
    val proxyEnabled: Boolean,
    val proxyName: String?
)

data class RateLimit(
    val requestsPerMinute: Int,
    val burst: Int
)

data class Path(
    val bookResultSelector: String,
    val bookRatingScoreSelector: String,
    val bookRatingCountSelector: String,
    val bookTitleSelector: String,
    val bookAuthorsSelector: String,
    val search: String,
    val includeAuthorsForSearch: Boolean,
    val titleSpaceSeparator: String
)

@ConfigurationProperties(prefix = "app")
data class ProxiesProperties(
    val proxies: List<ScrapingProxyProperties>
)

@ConfigurationProperties()
data class ScrapingProxyProperties(
    val name: String,
    var url: String = "",
    val headers: Map<String, String> = emptyMap(),
    var maxTimeout: Long = 0,
)

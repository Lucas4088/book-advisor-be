package io.github.luksal.ingestion.crawler.api.dto

data class Crawler(
    var id: Long? = null,
    val enabled: Boolean,
    val baseUrl: String,
    val rateLimit: RateLimit,
    val path: Path,
    val proxyEnabled: Boolean
)

data class RateLimit(
    val requestsPerMinute: Int,
    val burst: Int
)

data class Path(
    val bookResultSelector: String,
    val bookRatingCountSelector: String,
    val bookRatingScoreSelector: String,
    val search: String,
    val titleSpaceSeparator: String
)

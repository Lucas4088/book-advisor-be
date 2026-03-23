package io.github.luksal.ingestion.crawler.dto

data class CrawlerConfig(
    val id: Long?,
    val name: String,
    val enabled: Boolean,
    val baseUrl: String,
    val rateLimit: RateLimit,
    val headers: Map<String, String> = emptyMap(),
    val path: Path,
    val proxyEnabled: Boolean,
    val proxyName: String?,
    val proxySessionEnabled: Boolean,
    val forwardingProxyEnabled: Boolean
) {
    fun getRatingScoreSelector(): String =
        if (!path.isRatingAvailableOnSearch)
            path.bookRatingScoreSelector
        else requireNotNull(path.bookRatingScoreSearchSelector)

    fun getRatingCountSelector(): String =
        if (!path.isRatingAvailableOnSearch)
            path.bookRatingCountSelector
        else requireNotNull(path.bookRatingCountSearchSelector)

    fun getTitleSelector(): String =
        if (!path.isRatingAvailableOnSearch)
            path.bookTitleSelector
        else requireNotNull(path.bookTitleSearchSelector)

    fun getAuthorsSelector(): String =
        if (!path.isRatingAvailableOnSearch)
            path.bookAuthorsSelector
        else requireNotNull(path.bookAuthorsSearchSelector)

    fun getFirstElementSelector(): String? =
        if (path.isRatingAvailableOnSearch)
            path.bookFirstElementSearchSelector
        else null
}

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
    val searchPageLoadedSelector: String?,
    val isRatingAvailableOnSearch: Boolean,
    val bookFirstElementSearchSelector: String?,
    val bookRatingCountSearchSelector: String?,
    val bookRatingScoreSearchSelector: String?,
    val bookAuthorsSearchSelector: String?,
    val bookTitleSearchSelector: String?,
    val includeAuthorsForSearch: Boolean,
    val titleSpaceSeparator: String
)

data class CrawlerSearchDetails(
    var id: Long,
    var name: String,
    val enabled: Boolean,
    val baseUrl: String,
)

package io.github.luksal.statistics.api.dto

import io.github.luksal.commons.dto.EventStatus
import java.math.BigDecimal
import java.time.Instant


data class BookStatisticsDto(
    val authorCount: Long,
    val bookBasicInfoCount: Long,
    val bookDocumentCount: Long,
    val bookRecordCount: Long,
    val bookSyncPercentage: BigDecimal,
)

data class RatingEventStatusStatisticsDto(
    val total: Long,
    val values: List<RatingEventValueDto>
)

data class RatingEventValueDto(
    val status: String,
    val crawlerName: String?,
    val value: Long,
)

data class BookRatingStatisticsDto(
    val totalRatings: Long,
    val values: List<BookRatingValueDto>
)

data class BookRatingValueDto(
    val numberOfRatings: Int,
    val value: Long,
)

data class BookDetailsFetchedStatistics(
    val values: List<Map<String, Any>>
)

data class BookDetailsFetchedValueDto(
    val bucket: Instant,
    val sourceName: String? = null,
    val status: String? = null,
    val value: Long? = 0
)

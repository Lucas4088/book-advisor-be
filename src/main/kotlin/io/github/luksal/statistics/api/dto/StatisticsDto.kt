package io.github.luksal.statistics.api.dto


data class BookStatisticsDto(
    val authorCount: Long,
    val bookBasicInfoCount: Long,
    val bookDocumentCount: Long,
    val bookRecordCount: Long,
    val bookSyncPercentage: Double,
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

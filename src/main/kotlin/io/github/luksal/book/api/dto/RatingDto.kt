package io.github.luksal.book.api.dto

import java.math.BigDecimal

data class RatingResult(
    val averageRatingScore: BigDecimal,
    val totalRatingCount: Int,
    val ratings: List<RatingSingleSource>
)

data class RatingSingleSource(
    val ratingScore: BigDecimal,
    val ratingCount: Int?,
    val ratingSourceName: String
)
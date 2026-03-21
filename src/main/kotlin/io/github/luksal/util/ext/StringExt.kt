package io.github.luksal.util.ext

import com.google.common.hash.Hashing
import org.apache.commons.text.similarity.LevenshteinDistance
import org.apache.commons.text.similarity.LongestCommonSubsequenceDistance
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.MessageDigest


fun String.normalizeWhiteChars(): String =
    this.trim()
        .lowercase()
        .replace("\\s".toRegex(), "")

fun String.sha256(): String =
    MessageDigest.getInstance("SHA3-256")
        .digest(this.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

fun String.murmurHash3(): String =
    Hashing.murmur3_128()
        .hashString(this, Charsets.UTF_8)
        .toString()

fun String.normalizeStandardChars(): String =
    this.lowercase()
        .replace(Regex("[^\\p{L}\\p{Nd} ]"), "")
        .replace(Regex("\\p{Mn}"), "")
        .replace(Regex("\\s+"), " ")
        .trim()

fun String.levenshteinDistance(other: String): Int {
    val distance = LevenshteinDistance(5).apply(
        this.normalizeStandardChars(),
        other.normalizeStandardChars()
    )
    return if (distance == -1) Int.MAX_VALUE else distance
}

fun String.percentageLevenshteinDistance(other: String): BigDecimal {
    val thisNormalized = this.normalizeStandardChars()
    val otherNormalized = other.normalizeStandardChars()
    val distance = LevenshteinDistance(null).apply(
        thisNormalized,
        otherNormalized
    ).toBigDecimal()
    val shorterString = minOf(thisNormalized, otherNormalized, compareBy { it.length })
    val longerString = maxOf(thisNormalized, otherNormalized, compareBy { it.length })
    val longerStringLength = longerString.length.toBigDecimal()
    val basePercentage = longerStringLength
        .minus(distance)
        .divide(longerStringLength, 2, RoundingMode.HALF_UP)

    val longestSubsequenceFactor = LongestCommonSubsequenceDistance().apply(longerString, shorterString)
        .toBigDecimal()
        .divide(longerStringLength, 2, RoundingMode.HALF_UP)

    val extraFactor = if (longestSubsequenceFactor > BigDecimal.ZERO)
        basePercentage.multiply(longestSubsequenceFactor).coerceAtMost(BigDecimal.valueOf(1.0))
    else BigDecimal.ZERO

    return basePercentage.plus(extraFactor)

}
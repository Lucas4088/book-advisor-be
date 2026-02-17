package io.github.luksal.util.ext

import org.apache.commons.text.similarity.LevenshteinDistance
import java.security.MessageDigest


fun String.normalizeWhiteChars(): String =
    this.trim()
        .lowercase()
        .replace("\\s".toRegex(), "")

fun String.sha256(): String =
    MessageDigest.getInstance("SHA3-256")
        .digest(this.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

fun String.normalizeStandardChars(): String =
    this.lowercase()
        .replace(Regex("[^\\p{L}\\p{Nd} ]"), "")
        .replace(Regex("\\s+"), " ")
        .trim()

fun String.levenshteinDistance(other: String): Int {
    val distance = LevenshteinDistance(5).apply(
        this.normalizeStandardChars(),
        other.normalizeStandardChars()
    )
    return if (distance == -1) Int.MAX_VALUE else distance
}

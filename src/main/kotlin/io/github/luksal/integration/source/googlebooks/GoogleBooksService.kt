package io.github.luksal.integration.source.googlebooks

import io.github.luksal.exception.DailyQuotaExceededException
import io.github.luksal.integration.source.googlebooks.api.GoogleBooksClient
import io.github.luksal.integration.source.googlebooks.api.dto.GoogleBooksSearchResponse
import io.github.luksal.util.ext.logger
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import org.springframework.stereotype.Service

@Service
class GoogleBooksService(private val googleBooksClient: GoogleBooksClient) {

    private val log = logger()

    @CircuitBreaker(name = "search-googleBooksCircuitBreaker", fallbackMethod = "findBookDetailsFallback")
    @RateLimiter(name = "search-googleBooksRateLimiter")
    fun findBookDetails(title: String, authors: List<String>?): GoogleBooksSearchResponse? {
        val formattedTitle = title.trim().split("\\s+".toRegex()).joinToString("+")
        val formattedAuthors = authors?.joinToString("|") { it.trim().split("\\s+".toRegex()).joinToString("+") }
        val query = buildString {
            append("intitle:$formattedTitle")
            formattedAuthors?.let {
                append("+inauthor:$formattedAuthors")
            }
        }
        return try {
            googleBooksClient.searchBookDetails(query = query, maxResults = 1)
        } catch (e: DailyQuotaExceededException) {
            log.warn("Google books daily quota exceeded : SKIPPING")
            throw e
        }
    }

    private fun findBookDetailsFallback(title: String, authors: List<String>?, ex: Throwable): GoogleBooksSearchResponse? {
        log.warn("Google books service is unavailable. Falling back to null response. Error: ${ex.message}")
        return null
    }
}
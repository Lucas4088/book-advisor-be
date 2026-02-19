package io.github.luksal.integration.source.openlibrary

import io.github.luksal.integration.source.openlibrary.api.OpenLibraryClient
import io.github.luksal.integration.source.openlibrary.api.dto.OpenLibraryBookDetails
import io.github.luksal.integration.source.openlibrary.api.dto.OpenLibrarySearchResponse
import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import io.github.resilience4j.retry.annotation.Retry
import org.springframework.stereotype.Service

@Service
class OpenLibraryService(private val openLibraryClient: OpenLibraryClient) {

    companion object {
        private val FIELDS = listOf("title", "author_name", "first_publish_year", "key", "author_key", "language", "editions")
    }

    @RateLimiter(name = "search-openLibraryRateLimiter")
    @Retry(name = "search-openLibraryRetry")
    fun searchBy(startYear: Int, endYear: Int, lang: String, page: Int, limit: Int): OpenLibrarySearchResponse {
        val query = "publish_year:[$startYear TO $endYear]"
        return openLibraryClient.searchBooks(query, FIELDS.joinToString(","), lang, page, limit)
    }

    @RateLimiter(name = "search-openLibraryRateLimiter")
    @Retry(name = "search-openLibraryRetry")
    fun searchBy(title: String, authorName: String): OpenLibrarySearchResponse {
        val query = "title:\"$title\" author:\"$authorName\"$]"
        return openLibraryClient.searchBooks(query)
    }

    @RateLimiter(name = "get-openLibraryRateLimiter")
    @Retry(name = "get-openLibraryRetry")
    fun getBookDetails(id: String): OpenLibraryBookDetails {
        val id = "$id.json"
        return openLibraryClient.getBook(id)
    }
}
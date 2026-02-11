package io.github.luksal.book.googlebooks.api

import io.github.luksal.book.googlebooks.api.dto.GoogleBooksSearchResponse
import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import org.springframework.stereotype.Service

@Service
class GoogleBooksService(private val googleBooksClient: GoogleBooksClient) {

    @RateLimiter(name = "search-googleApiRateLimiter")
    fun findBookDetails(title: String, authors: List<String>?): GoogleBooksSearchResponse? {
        val formattedTitle = title.trim().split("\\s+".toRegex()).joinToString("+")
        val formattedAuthors = authors?.joinToString("|") { it.trim().split("\\s+".toRegex()).joinToString("+") }
        val query = buildString {
            append("intitle:$formattedTitle")
            formattedAuthors?.let {
                append("+inauthor:$formattedAuthors")
            }
        }
        return googleBooksClient.searchBookDetails(query = query, maxResults = 1)
    }
}
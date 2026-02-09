package io.github.luksal.book.openlibrary.api

import io.github.luksal.book.openlibrary.api.dto.OpenLibrarySearchResponse
import org.springframework.stereotype.Service

@Service
class OpenLibraryService(private val openLibraryClient: OpenLibraryClient) {

    companion object {
        private val FIELDS = listOf("title", "author_name", "first_publish_year", "key", "author_key", "language")
    }
    fun searchBooks(startYear: Int, endYear: Int, lang: String, page: Int, limit: Int): OpenLibrarySearchResponse {
        val query = "publish_year:[$startYear TO $endYear]"
        return openLibraryClient.searchBooks(query, FIELDS, lang, page, limit)
    }
}
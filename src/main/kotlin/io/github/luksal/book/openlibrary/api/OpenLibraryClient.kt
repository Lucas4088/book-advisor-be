package io.github.luksal.book.openlibrary.api

import io.github.luksal.book.openlibrary.api.dto.OpenLibrarySearchResponse
import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "openLibraryClient")
interface OpenLibraryClient {

    @GetMapping("/search.json")
    fun searchBooks(@RequestParam("q") query: String,
                    @RequestParam("fields") field: String,
                    @RequestParam("language") language: String,
                    @RequestParam("page") page: Int,
                    @RequestParam("limit") limit: Int): OpenLibrarySearchResponse
}
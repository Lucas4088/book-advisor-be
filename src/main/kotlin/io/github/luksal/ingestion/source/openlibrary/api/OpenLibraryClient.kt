package io.github.luksal.ingestion.source.openlibrary.api

import io.github.luksal.ingestion.source.openlibrary.api.dto.OpenLibrarySearchResponse
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
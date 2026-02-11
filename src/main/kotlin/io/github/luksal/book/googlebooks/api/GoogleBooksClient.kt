package io.github.luksal.book.googlebooks.api

import io.github.luksal.book.config.FeignGoogleBooksConfig
import io.github.luksal.book.googlebooks.api.dto.GoogleBooksSearchResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.FeignClientProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "googleBooksClient",
    configuration = [FeignClientProperties.FeignClientConfiguration::class, FeignGoogleBooksConfig::class]
)
interface GoogleBooksClient {

    @GetMapping("/books/v1/volumes", params = ["q", "max-results"])
    fun searchBookDetails(@RequestParam("q") query: String, @RequestParam("max-results") maxResults: Int): GoogleBooksSearchResponse
}
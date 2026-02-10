package io.github.luksal.book.googlebooks.api

import io.github.luksal.book.config.FeignGoogleBooksConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.FeignClientProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "googleBooksClient", configuration = [FeignClientProperties.FeignClientConfiguration::class, FeignGoogleBooksConfig::class])
interface GoogleBooksClient {

    @GetMapping("/volumes", params = ["intitle", "inauthor"])
    fun searchBookDetails(@RequestParam("intitle") title: String,)
}
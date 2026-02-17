package io.github.luksal.integration.source.archivebooks.api

import io.github.luksal.integration.source.archivebooks.api.dto.ArchiveBookDetailsResponse
import io.github.luksal.integration.source.archivebooks.api.dto.ArchiveSearchResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.FeignClientProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "archiveBooksClient",
    configuration = [FeignClientProperties.FeignClientConfiguration::class]
)
interface ArchiveBooksClient {

    @GetMapping("/advancedsearch.php", params = ["q", "output"])
    fun search(@RequestParam("q") q: String, @RequestParam("output", defaultValue = "json") output: String? = "json"): ArchiveSearchResponse?

    @GetMapping("/metadata/{id}")
    fun findById(@PathVariable("id") id: String): ArchiveBookDetailsResponse?
}
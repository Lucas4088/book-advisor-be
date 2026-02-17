package io.github.luksal.ingestion.fetcher

import io.github.luksal.util.ext.logger
import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class PageFetcher {

    companion object {
        val log = logger()
    }

    @RateLimiter(name = "page-fetcherRateLimiter")
    fun fetch(url: String): String {
        log.info("Fetching $url")
        return Jsoup.connect(url)
            .timeout(100_000)
            .get()
            .html()
    }
}
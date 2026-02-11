package io.github.luksal.ingestion.fetcher

import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class HttpFetcher {

    fun fetch(url: String): String {
        return Jsoup.connect(url)
            .userAgent("Mozilla/5.0")
            .timeout(10_000)
            .get()
            .html()
    }
}
package io.github.luksal.ingestion.fetcher

import io.github.luksal.util.ext.logger
import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class PageFetcher {

    companion object {
        val log = logger()
    }

    fun fetch(url: String): String {
        log.info("Fetching $url")
        return Jsoup.connect(url)
            .timeout(100_000)
            .get()
            .html()
    }
}
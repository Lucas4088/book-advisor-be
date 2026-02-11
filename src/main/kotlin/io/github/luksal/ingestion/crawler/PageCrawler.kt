package io.github.luksal.ingestion.crawler

import io.github.luksal.config.CrawlerSpecification
import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class PageCrawler {

    fun extractBookPageLink(html: String, crawlerSpecification: CrawlerSpecification): String? {
        val doc = Jsoup.parse(html)

        return doc.select(crawlerSpecification.path.bookResultSelector)
            ?.map{"${crawlerSpecification.baseUrl}${it.attr("href")}"}
            ?.first()
    }
}


package io.github.luksal.ingestion.crawler

import io.github.luksal.config.CrawlerSpecification
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class PageCrawler {

    fun extractBookPageLink(html: String, crawlerSpecification: CrawlerSpecification): String? {
        val doc = Jsoup.parse(html)

        //TODO handle element not found
        return doc.select(crawlerSpecification.path.bookResultSelector)
            .map{"${crawlerSpecification.baseUrl}${it.attr("href")}"}
            .firstOrNull()
    }

    fun extractRatingScore(html: String, crawlerSpecification: CrawlerSpecification): BigDecimal? {
        val doc = Jsoup.parse(html)
        val regex = Regex("""\d+[.,]\d+""")

        return doc.select(crawlerSpecification.path.bookRatingScoreSelector)
            .firstOrNull()
            ?.text()
            ?.toBigDecimalOrNull()
    }

    fun extractRatingCount(html: String, crawlerSpecification: CrawlerSpecification): Int? {
        val doc = Jsoup.parse(html)
        val regex = Regex("""\d{1,3}(?:,\d{3})*""")

        return doc.select(crawlerSpecification.path.bookRatingCountSelector)
            .firstOrNull()
            ?.text()
            ?.mapNotNull { regex.find(it.toString().trim())?.value?.toIntOrNull() }
            ?.firstOrNull()
    }
}



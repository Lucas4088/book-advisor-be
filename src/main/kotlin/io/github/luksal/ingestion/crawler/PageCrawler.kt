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

        return doc.selectFirst(crawlerSpecification.path.bookRatingScoreSelector)
            ?.text()
            ?.toBigDecimalOrNull()
    }

    fun extractRatingCount(html: String, crawlerSpecification: CrawlerSpecification): Int? {
        val doc = Jsoup.parse(html)
        val regex = Regex("""\d{1,3}(?:,\d{3})*""")

        return doc.selectFirst(crawlerSpecification.path.bookRatingCountSelector)
            //Todo nie dziaa dla goodreads
            ?.firstNotNullOfOrNull { regex.find(it.text().trim())?.value?.toIntOrNull() }
    }
}



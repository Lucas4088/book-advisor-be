package io.github.luksal.ingestion.crawler.service

import io.github.luksal.config.CrawlerSpecification
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class PageCrawler {

    fun extractBookPageUrl(html: String, crawlerSpecification: CrawlerSpecification): String? =
        Jsoup.parse(html)
            .select(crawlerSpecification.path.bookResultSelector)
            .map { "${crawlerSpecification.baseUrl}${it.attr("href")}" }
            .firstOrNull()

    fun extractRatingScore(html: String, crawlerSpecification: CrawlerSpecification): BigDecimal? =
        Jsoup.parse(html)
            .selectFirst(crawlerSpecification.path.bookRatingScoreSelector)
            ?.text()
            ?.toBigDecimalOrNull()

    fun extractTitle(html: String, crawlerSpecification: CrawlerSpecification) =
        Jsoup.parse(html)
            .selectFirst(crawlerSpecification.path.bookTitleSelector)
            ?.text()
            .orEmpty()

    fun extractAuthors(html: String, crawlerSpecification: CrawlerSpecification): List<String> =
        Jsoup.parse(html)
            .select(crawlerSpecification.path.bookAuthorsSelector)
            .map { it.text() }

    fun extractRatingCount(html: String, crawlerSpecification: CrawlerSpecification): Int? {
        val doc = Jsoup.parse(html)
        val regex = Regex("""\d+""")

        return doc.selectFirst(crawlerSpecification.path.bookRatingCountSelector)
            ?.firstNotNullOfOrNull { regex.find(it.text().trim().replace(Regex("""[.,]"""), ""))?.value?.toIntOrNull() }
    }
}
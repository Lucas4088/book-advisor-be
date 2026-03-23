package io.github.luksal.ingestion.crawler.service

import io.github.luksal.ingestion.crawler.dto.CrawlerConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class PageCrawler {

    fun extractBookPageUrl(html: String, crawlerConfig: CrawlerConfig): String? =
        Jsoup.parse(html)
            .select(crawlerConfig.path.bookResultSelector)
            .map { "${crawlerConfig.baseUrl}${it.attr("href")}" }
            .firstOrNull()

    fun isSearchPageLoaded(html: String, crawlerConfig: CrawlerConfig): Boolean {
        if(crawlerConfig.path.searchPageLoadedSelector == null) {
            return true
        }
        return Jsoup.parse(html)
            .select(crawlerConfig.path.searchPageLoadedSelector)
            .firstOrNull()
            .let { it != null }
    }

    fun extractRatingScore(html: String, crawlerConfig: CrawlerConfig): BigDecimal? {
        return selectFirstSearchResult(Jsoup.parse(html), crawlerConfig)
            .selectFirst(crawlerConfig.getRatingScoreSelector())
            ?.text()
            ?.toBigDecimalOrNull()
    }

    fun extractRatingCount(html: String, crawlerConfig: CrawlerConfig): Int? {
        val regex = Regex("""\d+""")
        return selectFirstSearchResult(Jsoup.parse(html), crawlerConfig)
            .selectFirst(crawlerConfig.getRatingCountSelector())
            ?.firstNotNullOfOrNull { regex.find(it.text().trim().replace(Regex("""[.,]"""), ""))?.value?.toIntOrNull() }
    }

    fun extractTitle(html: String, crawlerConfig: CrawlerConfig): String {
        return selectFirstSearchResult(Jsoup.parse(html), crawlerConfig)
            .selectFirst(crawlerConfig.getTitleSelector())
            ?.text()
            .orEmpty()
    }

    fun extractAuthors(html: String, crawlerConfig: CrawlerConfig): List<String> {
        return selectFirstSearchResult(Jsoup.parse(html), crawlerConfig)
            .select(crawlerConfig.getAuthorsSelector())
            .map { it.text() }
    }

    private fun selectFirstSearchResult(document: Document, crawlerConfig: CrawlerConfig): Element =
        crawlerConfig.getFirstElementSelector()
            ?.let { document.selectFirst(it) }
            ?: document

}
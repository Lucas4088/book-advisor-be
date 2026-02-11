package io.github.luksal.ingestion.service

import io.github.luksal.config.AppProperties
import io.github.luksal.ingestion.crawler.PageCrawler
import io.github.luksal.ingestion.fetcher.HttpFetcher
import io.github.luksal.util.ext.logger
import org.springframework.stereotype.Service

@Service
class BookIngestionService(
    private val fetcher: HttpFetcher,
    private val pageCrawler: PageCrawler,
    private val appProperties: AppProperties
) {

    val log = logger()

    fun crawlAndIngest(title: String) {
        appProperties.crawlers.forEach { crawlerSpec ->
            val searchTitle = title.replace(" ", crawlerSpec.path.titleSpaceSeparator)
            val searchPageHtml = fetcher.fetch("${crawlerSpec.baseUrl}${crawlerSpec.path.search}${searchTitle}")

            val bookPage = pageCrawler.extractBookPageLink(searchPageHtml, crawlerSpec)?.let {
                log.info(it)
                fetcher.fetch(it)
            }
        }
    }
}
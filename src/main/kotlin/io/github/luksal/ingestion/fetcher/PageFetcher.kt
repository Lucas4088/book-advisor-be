package io.github.luksal.ingestion.fetcher

import io.github.luksal.config.ScrapingProxyProperties
import io.github.luksal.util.ext.logger
import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import io.github.resilience4j.retry.annotation.Retry
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class PageFetcher(private val properties: ScrapingProxyProperties) {

    companion object {
        val log = logger()
    }

    @Retry(name = "page-fetcherRetry")
    @RateLimiter(name = "page-fetcherRateLimiter")
    fun fetch(url: String): String {
        log.info("Fetching $url")
        val body = ObjectMapper().writeValueAsString(
            RequestData(
                cmd = "sessions.create",
                url = url,
                maxTimeout = properties.maxTimeout.toInt()
            )
        )
     /*   return Jsoup.connect(properties.url)
            .header("Content-Type", "application/json")
            .requestBody(ObjectMapper().writeValueAsString(
                RequestData(
                    cmd = "request.get",
                    url = url,
                    maxTimeout = properties.maxTimeout.toInt()
                )
            ))
            .timeout(100_000)
            .post()
            .html()*/

        return fetchWithSession(properties.url, url, properties.maxTimeout.toInt())

       /* val json = Jsoup.connect("http://proxy.localhost/v1")
            .header("Content-Type", "application/json")
            .requestBody(body)
            .timeout(100_000)
            .post()
            .body()
            .text()

        return ObjectMapper().readTree(json).path("session").asText()
*/    }

    fun fetchWithSession(proxyUrl: String, targetUrl: String, maxTimeout: Int): String {
        val mapper = ObjectMapper()

        val createBody = mapper.writeValueAsString(
            RequestData(cmd = "sessions.create", maxTimeout = maxTimeout)
        )
        val createJson = Jsoup.connect(proxyUrl)
            .header("Content-Type", "application/json")
            .requestBody(createBody)
            .timeout(maxTimeout)
            .post()
            .body()
            .text()

        val sessionId = mapper.readTree(createJson).path("session").asText()

        val requestBody = mapper.writeValueAsString(
            RequestData(cmd = "request.get", url = targetUrl, session = sessionId, maxTimeout = maxTimeout)
        )
        val requestJson = Jsoup.connect(proxyUrl)
            .header("Content-Type", "application/json")
            .requestBody(requestBody)
            .timeout(maxTimeout)
            .post()
            .body()
            .text()

        return mapper.readTree(requestJson).path("solution").path("response").asText()
    }

    data class RequestData(
        val cmd: String,
        val url: String? = null,
        val session: String? = null,
        val maxTimeout: Int

    )
}
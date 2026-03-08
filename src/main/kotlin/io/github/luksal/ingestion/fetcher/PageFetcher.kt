package io.github.luksal.ingestion.fetcher

import com.fasterxml.jackson.annotation.JsonInclude
import io.github.luksal.config.ScrapingProxyProperties
import io.github.luksal.util.ext.logger
import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import io.github.resilience4j.retry.annotation.Retry
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import java.io.File

@Component
class PageFetcher(
    private val restTemplate: RestTemplate,
    private var session: String? = null
) {

    companion object {
        val log = logger()
    }

    @Retry(name = "page-fetcherRetry")
    @RateLimiter(name = "page-fetcherRateLimiter")
    fun fetchRemoteProxy(url: String, proxy: ScrapingProxyProperties): String? {
        log.info("Fetching $url")

        val body = ObjectMapper().writeValueAsString(
            RemoteProxyRequestData(
                url = url
            )
        )

        val response = Jsoup.connect(proxy.url)
            .header("Content-Type", "application/json")
            .header("Authorization", "Basic VTAwMDAzNjQ3Nzc6UFdfMTkxOTc5ZjU1NjJkNDI3YTRhMjRiYjBlMTIzODRmNzlj")
            .header("Accept", "application/json")
            .method(Connection.Method.POST)
            .requestBody(body)
            .timeout(100_000)
            .ignoreContentType(true)
            .execute()

          return response.body().let {
              JsonMapper().readTree(it)
                  .get("results")?.get(0)
                  ?.get("content")?.asString()
          }.also {
              it?.let { t->
                  val file = File("fetcher.html").apply {
                      writeText(t, Charsets.UTF_8)
                  }
              }
          }
    }


    fun fetchNoProxy(url: String): String =
        Jsoup.connect(url)
            .timeout(10_000)
            .execute()
            .body()

    fun fetchLocalProxy(url: String, proxy: ScrapingProxyProperties): String {
        return fetchLocalProxy(proxy.url, url, proxy.maxTimeout.toInt(), null)
    }

     fun fetchLocalProxyWitSession(url: String, proxy: ScrapingProxyProperties): String {
        if (session == null) {
            session = createSession(proxy.url, proxy.maxTimeout.toInt())
                ?: throw RuntimeException("Failed to create session for proxy ${proxy.url}")
        }

        return fetchLocalProxy(proxy.url, url, proxy.maxTimeout.toInt(), session)
    }

    private fun fetchLocalProxy(proxyUrl: String, targetUrl: String, maxTimeout: Int, session: String?): String {
        val requestBody = ObjectMapper().writeValueAsString(
            ProxyLocalRequestData(
                session = session,
                cmd = "request.get",
                url = targetUrl,
                maxTimeout = maxTimeout
            )
        )
        val requestJson = Jsoup.connect(proxyUrl)
            .header("Content-Type", "application/json")
            .timeout(maxTimeout)
            .method(Connection.Method.POST)
            .requestBody(requestBody)
            .timeout(100_000)
            .ignoreContentType(true)
            .maxBodySize(0) //Jsoup can truncate huge response body, so we set it to 0 to disable truncation
            .execute()
            .body().also {
                JsonMapper().readTree(it)
                    .get("solution")?.get("response")?.asString()?.let { t ->
                        val file = File("fetcher.html").apply {
                            writeText(t, Charsets.UTF_8)
                        }
                    }
            }

        return JsonMapper().readTree(requestJson).path("solution").path("response").asString()
    }

    private fun createSession(proxyUrl: String, maxTimeout: Int): String? {
        return restTemplate.postForObject(
            proxyUrl, ProxyLocalRequestData(cmd = "sessions.create", maxTimeout = maxTimeout),
            FlareSolverrSessionResponse::class.java
        )?.session
    }


    data class RemoteProxyRequestData(
        val url: String? = null,
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class ProxyLocalRequestData(
        val cmd: String,
        val session: String? = null,
        val url: String? = null,
        val maxTimeout: Int
    )

    data class FlareSolverrSessionResponse(
        val status: String,
        val message: String,
        val session: String,
        val startTimestamp: Long,
        val endTimestamp: Long,
        val version: String
    )

    data class Proxy(
        val url: String
    )
}
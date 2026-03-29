package io.github.luksal.ingestion.fetcher

import com.fasterxml.jackson.annotation.JsonInclude
import io.github.luksal.config.ProxiesProperties
import io.github.luksal.util.ext.logger
import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import io.github.resilience4j.retry.annotation.Retry
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import java.io.File
import kotlin.random.Random

@Component
class PageFetcher(
    private val proxiesProperties: ProxiesProperties,
) {

    companion object {
        val log = logger()
        const val REMOTE_PROXY_NAME = "decodo"
        const val LOCAL_PROXY_NAME = "flaresolverr"
    }

    @Retry(name = "page-fetcherRetry")
    @RateLimiter(name = "page-fetcherRateLimiter")
    fun fetchRemoteProxy(url: String): String? {
        log.info("Fetching $url")

        val proxy = proxiesProperties.proxies.firstOrNull { it.name == REMOTE_PROXY_NAME }
            ?: throw IllegalStateException("Proxy '$REMOTE_PROXY_NAME' not found for crawler")


        val body = ObjectMapper().writeValueAsString(
            RemoteProxyRequestData(
                url = url
            )
        )
        var connection = Jsoup.connect(proxy.url)

        proxy.headers.forEach { (string, string1) ->
            connection = connection.header(string, string1)
        }

        val response = connection
            .method(Connection.Method.POST)
            .requestBody(body)
            .timeout(proxy.maxTimeout.toInt())
            .ignoreContentType(true)
            .execute()

        return response.body().let {
            JsonMapper().readTree(it)
                .get("results")?.get(0)
                ?.get("content")?.asString()
        }
    }

    fun fetchNoProxy(url: String): String =
        Jsoup.connect(url)
            .timeout(10_000)
            .execute()
            .body()

    private fun fetchLocalProxy(
        proxyUrl: String,
        targetUrl: String,
        maxTimeout: Int,
        session: String? = null,
        forwardingProxyUrl: String? = null
    ): String {
        val requestBody = ObjectMapper().writeValueAsString(
            ProxyLocalRequestData(
                session = session,
                cmd = "request.get",
                url = targetUrl,
                disableMedia = false,
                sessionTttlMinutes = 30,
                maxTimeout = maxTimeout,
                proxy = forwardingProxyUrl?.let { Proxy(it) }
            )
        )
        return runCatching {
            val resJson = Jsoup.connect(proxyUrl)
                .header("Content-Type", "application/json")
                .timeout(maxTimeout)
                .method(Connection.Method.POST)
                .requestBody(requestBody)
                .timeout(100_000)
                .ignoreContentType(true)
                .maxBodySize(0) //Jsoup can truncate huge response body, so we set it to 0 to disable truncation
                .execute()
                .body().also {
                    File("Search_page.html").apply {
                        writeText(it)
                    }
                }

            JsonMapper().readTree(resJson)
                .path("solution")
                .path("response")
                .asString()
        }.onFailure {
            log.error("Error while calling proxy", it)
        }.getOrThrow()

    }


    fun fetchLocalProxy(url: String, proxyForwardingEnabled: Boolean): String {
        val proxy = proxiesProperties.proxies.firstOrNull { it.name == LOCAL_PROXY_NAME }
            ?: throw IllegalStateException("Proxy '$LOCAL_PROXY_NAME' not found for crawler")

        var forwardingProxyUrl: String? = null
        if (proxyForwardingEnabled) {
            forwardingProxyUrl = proxy.forwardingProxiesUrls[Random.nextInt(0, proxy.forwardingProxiesUrls.size)]
        }
        log.info("Forwarding proxy ${forwardingProxyUrl?.take(15)}")
        return fetchLocalProxy(proxy.url, url, proxy.maxTimeout.toInt(), null, forwardingProxyUrl)
    }

    data class RemoteProxyRequestData(
        val url: String? = null,
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class ProxyLocalRequestData(
        val cmd: String,
        val session: String? = null,
        val url: String? = null,
        val disableMedia: Boolean = false,
        val sessionTttlMinutes: Int = 1,
        val maxTimeout: Int? = 0,
        val proxy: Proxy? = null,
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
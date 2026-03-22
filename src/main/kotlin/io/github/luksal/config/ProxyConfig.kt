package io.github.luksal.config

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "app")
data class ProxiesProperties(
    val proxies: List<ScrapingProxyProperties>
)

data class ScrapingProxyProperties(
    val name: String,
    val url: String = "",
    val forwardingProxiesUrls: List<String> = emptyList(),
    val headers: Map<String, String> = emptyMap(),
    val maxTimeout: Long = 0,
)
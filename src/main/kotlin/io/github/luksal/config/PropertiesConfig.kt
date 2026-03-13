package io.github.luksal.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(CrawlerProperties::class, ProxiesProperties::class, GoogleBooksAuthProperties::class)
class PropertiesConfig {
}
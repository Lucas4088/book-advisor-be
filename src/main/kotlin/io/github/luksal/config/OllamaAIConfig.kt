package io.github.luksal.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class OllamaAIConfig {

    @Bean
    fun ollamaWebClient(@Value($$"${app.ai.ollama.url}") ollamaHostUrl: String) =
        WebClient.builder()
            .baseUrl(ollamaHostUrl)
            .build()
}
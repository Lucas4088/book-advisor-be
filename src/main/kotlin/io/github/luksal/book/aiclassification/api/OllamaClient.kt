package io.github.luksal.book.aiclassification.api

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class OllamaClient(private val ollamaWebClient: WebClient) {

    suspend fun generateEmbedding(model: String, prompt: String): List<Double> =
        ollamaWebClient.post()
            .uri("/api/embeddings")
            .bodyValue(mapOf("model" to model, "prompt" to prompt))
            .retrieve()
            .awaitBody<EmbeddingResponse>()
            .embedding

    suspend fun generateChatCompletion(model: String, prompt: String): String =
        ollamaWebClient.post()
            .uri("/api/generate")
            .bodyValue(
                mapOf(
                    "model" to model,
                    "prompt" to prompt,
                    "stream" to false
                )
            )
            .retrieve()
            .awaitBody<ChatResponse>()
            .response


    private data class EmbeddingResponse(val embedding: List<Double>)
    private data class ChatResponse(val response: String)

}
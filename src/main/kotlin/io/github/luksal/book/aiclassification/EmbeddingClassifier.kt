package io.github.luksal.book.aiclassification

import io.github.luksal.book.aiclassification.api.OllamaClient
import io.github.luksal.book.aiclassification.service.GenreClassifierService
import org.springframework.stereotype.Component
import kotlin.math.sqrt

@Component
class EmbeddingClassifier(
    private val client: OllamaClient,
) {

    private val genreEmbeddings = mutableMapOf<String, List<Double>>()

    companion object {
        const val THRESHOLD = 0.75
        const val MAX_GENRES = 4
    }

    suspend fun init() {
        GenreClassifierService.GENERAL_GENRES.forEach { genre ->
            val embedding = client.generateEmbedding(
                model = "mxbai-embed-large",
                prompt = genre
            )
            genreEmbeddings[genre] = embedding
        }
    }

    suspend fun classify(
        title: String,
        subjects: List<String>,
        description: String,
    ): List<Pair<String, Double>> {

        val prompt = """
                    Title: $title
                    Subjects: ${subjects.joinToString(", ")}
                    Description: $description
                    """.trimIndent()
        val bookEmbedding = try {
            client.generateEmbedding(
                model = "mxbai-embed-large",
                prompt = prompt
            )
        } catch (ex: Exception) {
            return emptyList()
        }

        return genreEmbeddings
            .map { (genre, embedding) ->
                genre to cosineSimilarity(bookEmbedding, embedding)
            }
            .filter { it.second >= THRESHOLD }
            .sortedByDescending { it.second }
            .take(MAX_GENRES)
    }


    fun cosineSimilarity(a: List<Double>, b: List<Double>): Double {
        val dot = a.zip(b).sumOf { (x, y) -> x * y }
        val normA = sqrt(a.sumOf { it * it })
        val normB = sqrt(b.sumOf { it * it })

        if (normA == 0.0 || normB == 0.0) return 0.0

        return dot / (normA * normB)
    }
}
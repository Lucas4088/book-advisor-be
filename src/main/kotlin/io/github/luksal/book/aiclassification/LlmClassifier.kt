package io.github.luksal.book.aiclassification

import io.github.luksal.book.aiclassification.api.OllamaClient
import io.github.luksal.book.aiclassification.service.GenreClassifierService
import org.springframework.stereotype.Component

@Component
class LlmClassifier(
    private val ollamaClient: OllamaClient
) {
    companion object {
        const val MAX_GENRES = 4
    }

    suspend fun classify(
        title: String,
        subjects: List<String>,
        description: String
    ): List<String> =
        ollamaClient.generateChatCompletion(
            model = "llama3",
            prompt = """
                Classify the book into MAXIMUM $MAX_GENRES genres.

                Allowed genres:
                ${GenreClassifierService.GENERAL_GENRES.joinToString(", ")}
                
                Rules:
                - Return MAXIMUM $MAX_GENRES genres
                - Only from the allowed list
                - No explanations
                - No extra text
                - Output must be a single line
                
                Format:
                Genre1,Genre2,Genre3,Genre4
                
                Book:
                Title: $title
                Subjects: ${subjects.joinToString(", ")}
                Description: $description
                """.trimIndent()
        ).split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filter { it in GenreClassifierService.GENERAL_GENRES }
}
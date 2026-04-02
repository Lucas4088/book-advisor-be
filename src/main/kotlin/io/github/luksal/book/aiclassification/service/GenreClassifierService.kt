package io.github.luksal.book.aiclassification.service

import io.github.luksal.book.aiclassification.EmbeddingClassifier
import io.github.luksal.book.aiclassification.LlmClassifier
import io.github.luksal.book.model.Book
import org.springframework.stereotype.Service

@Service
class GenreClassifierService(
    private val embeddingClassifier: EmbeddingClassifier,
    private val llmClassifier: LlmClassifier
) {

    companion object {
        val GENERAL_GENRES = listOf(
            "Horror",
            "Thriller",
            "Mystery",
            "Romance",
            "Historical Fiction",
            "Literary Fiction",
            "Adventure",
            "Dystopian",
            "Biography",
            "History",
            "Science",
            "Technology",
            "Psychology",
            "Self-Help",
            "Business",
            "Philosophy",
            "Politics",
            "True Crime",
            "Health",
            "Religion",
            "Science Fiction",
            "Fantasy",
            "Young Adult",
            "Children",
            "Classic",
            "Graphic Novel",
            "Poetry",
            "Memoir",
            "Travel",
        )
    }


    suspend fun classifyBookGenre(
        book: Book
    ): List<String> =
        classifyBookGenre(
            title = book.title,
            subjects = book.genres.map { it.name },
            description = book.description
        )

    suspend fun classifyBookGenre(
        title: String,
        subjects: List<String>,
        description: String?
    ): List<String> {
        val embeddingResult = embeddingClassifier.classify(
            title = title,
            subjects = subjects,
            description = description ?: ""
        )
        if (embeddingResult.size < 3) {
            return llmClassifier.classify(
                title = title,
                subjects = subjects,
                description = description ?: ""
            )
        }
        return embeddingResult.map { it.first }
    }
}
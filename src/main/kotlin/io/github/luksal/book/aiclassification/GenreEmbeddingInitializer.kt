package io.github.luksal.book.aiclassification

import kotlinx.coroutines.runBlocking
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class GenreEmbeddingInitializer(
    private val embeddingClassifier: EmbeddingClassifier
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        runBlocking {
            embeddingClassifier.init()
        }
    }
}
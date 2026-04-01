package io.github.luksal.book.aiclassification.job

import io.github.luksal.book.aiclassification.service.GenreClassifierService
import io.github.luksal.book.model.Genre
import io.github.luksal.book.model.Tag
import io.github.luksal.book.service.BookService
import io.github.luksal.util.ext.logger
import kotlinx.coroutines.runBlocking
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class BookGenresClassifierJob(
    private val genreClassifierService: GenreClassifierService,
    private val bookService: BookService,
    private val redisTemplate: RedisTemplate<Any, Any>,
) {

    companion object {
        val log = logger()
    }

    @Scheduled(fixedDelay = 2, timeUnit = TimeUnit.SECONDS)
    fun run() = runBlocking {
        val currentPage = redisTemplate.opsForValue()["booksindex:genreclassifier"]
            ?.toString()?.toIntOrNull() ?: 0

        bookService.getBooksForGenreClassification()
            .forEach { book ->
                val result = genreClassifierService.classifyBookGenre(book)
                book.tags = book.genres.map { Tag(it.id, it.name) }
                book.genres = result.map { Genre(name = it) }
                bookService.updateBook(book)
                log.info("Book ${book.title} genres: $result")
            }

        redisTemplate.opsForValue()["booksindex:genreclassifier"] = currentPage + 1
    }
}
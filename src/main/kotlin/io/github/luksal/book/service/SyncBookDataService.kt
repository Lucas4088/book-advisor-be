package io.github.luksal.book.service

import com.github.pemistahl.lingua.api.LanguageDetector
import io.github.luksal.book.db.jpa.event.SyncBookEventJpaRepository
import io.github.luksal.book.mapper.BookMapper.mapToEntity
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class SyncBookDataService(
    private val syncBookEventJpaRepository: SyncBookEventJpaRepository,
    private val bookService: BookService,
    private val languageDetector: LanguageDetector,
) {

    //TODO next do this with kafka connect :))
    fun syncBookData() {
        var pageNumber = 0
        do {
            val page = PageRequest.of(pageNumber, 50)
            val ids = syncBookEventJpaRepository.findAllPending(page).map { it.bookId }.toList()
            bookService.getBookDocumentByIds(ids)
                .map { it.mapToEntity(languageDetector.detectLanguageOf(it.title).name) }
                .let { bookService.saveBookEntities(it) }
            pageNumber++
        } while (ids.isNotEmpty())

    }
}
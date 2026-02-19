package io.github.luksal.book.service

import io.github.luksal.book.db.jpa.event.SyncBookEventJpaRepository
import io.github.luksal.book.mapper.BookMapper
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class SyncBookDataService(
    private val syncBookEventJpaRepository: SyncBookEventJpaRepository,
    private val bookService: BookService
) {

    //TODO next do this with kafka connect :))
    fun syncBookData() {
        var pageNumber = 0
        do {
            val page = PageRequest.of(pageNumber, 50)
            val ids = syncBookEventJpaRepository.findAllPending(page).map { it.bookId }.toList()
            bookService.getBookDocumentByIds(ids).map { BookMapper.mapToEntity(it) }.let {
                bookService.saveBookEntities(it)
            }
            pageNumber++
        } while (ids.isNotEmpty())

    }
}
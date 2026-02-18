package io.github.luksal.book.service

import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.db.document.book.BookDocument
import io.github.luksal.book.db.document.book.repository.BookDocumentRepository
import io.github.luksal.book.db.document.bookbasicinfo.BookBasicInfoDocument
import io.github.luksal.book.db.document.bookbasicinfo.repository.BookBasicInfoDocumentRepository
import io.github.luksal.book.db.jpa.BookJpaRepository
import io.github.luksal.book.db.jpa.SyncBookEventJpaRepository
import io.github.luksal.book.db.jpa.model.BookEntity
import io.github.luksal.book.db.jpa.model.event.EventStatus
import io.github.luksal.book.db.jpa.model.event.SyncBookEventEntity
import io.github.luksal.book.mapper.BookMapper
import io.github.luksal.book.model.Book
import io.github.luksal.book.model.BookUpdate
import io.github.luksal.book.service.dto.BookSearchCriteriaDto
import io.github.luksal.integration.source.openlibrary.api.dto.OpenLibraryDoc
import io.github.luksal.util.ext.logger
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Instant


@Service
class BookService(
    private val bookJpaRepository: BookJpaRepository,
    private val bookBasicInfoDocumentRepository: BookBasicInfoDocumentRepository,
    private val bookDocumentRepository: BookDocumentRepository,
    private val syncBookEventJpaRepository: SyncBookEventJpaRepository
) {

    private val log = logger()

    fun searchBookDocuments(criteria: BookSearchCriteriaDto, pageable: Pageable): Page<BookSearchResponse> {
        return bookDocumentRepository.search(
            title = criteria.title,
            startYear = criteria.publishedYearRange.first,
            endYear = criteria.publishedYearRange.last,
            genres = criteria.genres?.map { it.name },
            pageable = pageable
        ).map { BookMapper.map(it) }
    }

    fun getBookById(id: Long): BookSearchResponse {
        return bookJpaRepository.findById(id).map { BookMapper.map(it) }
            .orElseThrow()
    }

    fun getBookDocumentByIds(ids: List<String>): List<BookDocument> {
        return bookDocumentRepository.findAllById(ids)
    }

    @Transactional
    fun saveBookDocuments(books: List<Book>) {
        if (books.isEmpty()) {
            log.info("No book documents to save")
            return
        }
        books.map {
            SyncBookEventEntity(
                processed = false,
                bookId = it.id,
                status = EventStatus.NEW,
                timestamp = Instant.now().toEpochMilli(),
            )
        }.let {
            syncBookEventJpaRepository.saveAll(it)
        }
        bulkSaveNoDuplicatesBooks(books)
    }

    fun saveBookEntities(books: List<BookEntity>) {
        if (books.isEmpty()) {
            log.info("No book entities to save")
            return
        }
        bookJpaRepository.saveAll(books)
    }

    fun updateBook(bookUpdate: BookUpdate): String? =
        bookDocumentRepository.update(bookUpdate)

    fun saveBookBasicInfo(bookBasicInfo: List<OpenLibraryDoc>, lang: String): Int {
        return bookBasicInfo.map { it.toBasicInfoDocument(lang) }.let {
            if (it.isEmpty()) {
                log.info("No book basic info to save for lang=$lang")
                return@let 0
            }
            bulkSaveNoDuplicatesBasicBookInfo(it)
        }
    }

    fun updateBookBasicInfo(bookBasicInfoDocument: List<BookBasicInfoDocument>) =
        bookBasicInfoDocumentRepository.saveAll(bookBasicInfoDocument)


    fun getUnprocessedBookBasicInfo(page: Pageable): Page<BookBasicInfoDocument> =
        bookBasicInfoDocumentRepository.findByProcessed(false, page)

    private fun bulkSaveNoDuplicatesBooks(books: List<Book>) =
        bookDocumentRepository.saveBulkWithDeduplication(books.map { BookMapper.map(it) })


    private fun bulkSaveNoDuplicatesBasicBookInfo(documents: List<BookBasicInfoDocument>): Int =
        bookBasicInfoDocumentRepository.saveBulkWithDeduplication(documents).size

}
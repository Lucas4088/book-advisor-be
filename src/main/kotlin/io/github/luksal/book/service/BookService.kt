package io.github.luksal.book.service

import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.db.document.book.BookDocument
import io.github.luksal.book.db.document.book.repository.BookDocumentRepository
import io.github.luksal.book.db.document.bookbasicinfo.BookBasicInfoDocument
import io.github.luksal.book.db.document.bookbasicinfo.repository.BookBasicInfoDocumentRepository
import io.github.luksal.book.db.jpa.BookJpaRepository
import io.github.luksal.book.db.jpa.model.BookEntity
import io.github.luksal.book.mapper.BookMapper
import io.github.luksal.book.model.*
import io.github.luksal.book.service.dto.BookSearchCriteriaDto
import io.github.luksal.integration.source.openlibrary.api.dto.OpenLibraryDoc
import io.github.luksal.util.ext.logger
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service


@Service
class BookService(
    private val bookJpaRepository: BookJpaRepository,
    private val bookBasicInfoDocumentRepository: BookBasicInfoDocumentRepository,
    private val bookDocumentRepository: BookDocumentRepository,
    private val publisher: ApplicationEventPublisher
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

    fun getBooksByIds(bookPublicIds: List<String>): List<BookSearchResponse> =
        bookJpaRepository.findAllById(bookPublicIds).map { BookMapper.map(it) }

    //TODO think more of this failover strategy
    fun getBookById(id: String): BookSearchResponse =
        bookJpaRepository.findById(id)
            .orElse(bookDocumentRepository.findById(id).map { BookMapper.mapToEntity(it) }.orElseThrow())
            .let { BookMapper.map(it) }


    fun getBookDocumentByIds(ids: List<String>): List<BookDocument> =
        bookDocumentRepository.findAllById(ids)

    @Transactional
    fun saveBookDocuments(books: List<Book>) {
        if (books.isEmpty()) {
            log.info("No book documents to save")
            return
        }
        books.forEach { event ->
            publisher.publishEvent(BookDocumentSavedEvent(bookId = event.id))
        }
        bulkSaveNoDuplicatesBooks(books)
    }

    fun saveBookEntities(books: List<BookEntity>) {
        if (books.isEmpty()) {
            log.info("No book entities to save")
            return
        }
        bookJpaRepository.saveAll(books)
        books.forEach {
            publisher.publishEvent(BookEntitySavedEvent(bookId = it.id!!))
        }
    }

    fun updateBook(bookUpdate: BookUpdate): String? =
        bookDocumentRepository.update(bookUpdate)

    @Transactional
    fun saveBookBasicInfo(bookBasicInfo: List<BookBasicInfoDocument>): Int =
        bookBasicInfo.let { bookBasicInfo ->
            if (bookBasicInfo.isEmpty()) {
                log.info("No book basic info to save")
                return@let 0
            }
            bookBasicInfo.forEach {
                publisher.publishEvent(BookBasicInfoDocumentSavedEvent(bookId = it.publicId))
            }
            bulkSaveNoDuplicatesBasicBookInfo(bookBasicInfo)
        }

    @Transactional
    fun saveBookBasicInfo(bookBasicInfo: List<OpenLibraryDoc>, lang: String): Int =
        bookBasicInfo.map { it.toBasicInfoDocument(lang) }.let { bookBasicInfo ->
            if (bookBasicInfo.isEmpty()) {
                log.info("No book basic info to save for lang=$lang")
                return@let 0
            }
            bookBasicInfo.forEach {
                publisher.publishEvent(BookBasicInfoDocumentSavedEvent(bookId = it.publicId))
            }
            bulkSaveNoDuplicatesBasicBookInfo(bookBasicInfo)
        }

    fun updateBookBasicInfo(bookBasicInfoDocument: List<BookBasicInfoDocument>) =
        bookBasicInfoDocumentRepository.saveAll(bookBasicInfoDocument)


    fun getBookBasicInfo(bookIds: List<String>, page: Pageable): Page<BookBasicInfoDocument> =
        bookBasicInfoDocumentRepository.findAllByPublicIdIn(bookIds, page)

    @Transactional
    fun deleteAllBookDetails() {
        bookJpaRepository.deleteAll()
        bookDocumentRepository.deleteAll()
    }

    fun deleteAllBookBasicInfo() {
        bookBasicInfoDocumentRepository.deleteAll()
    }

    private fun bulkSaveNoDuplicatesBooks(books: List<Book>) =
        bookDocumentRepository.saveBulkWithDeduplication(books.map { BookMapper.map(it) })

    private fun bulkSaveNoDuplicatesBasicBookInfo(documents: List<BookBasicInfoDocument>): Int =
        bookBasicInfoDocumentRepository.saveBulkWithDeduplication(documents).size

}
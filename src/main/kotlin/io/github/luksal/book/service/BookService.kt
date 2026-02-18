package io.github.luksal.book.service

import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.db.document.book.repository.BookDocumentRepository
import io.github.luksal.book.db.document.bookbasicinfo.BookBasicInfoDocument
import io.github.luksal.book.db.document.bookbasicinfo.repository.BookBasicInfoDocumentRepository
import io.github.luksal.book.db.jpa.BookJpaRepository
import io.github.luksal.book.mapper.BookMapper
import io.github.luksal.book.model.Book
import io.github.luksal.book.model.BookUpdate
import io.github.luksal.book.service.dto.BookSearchCriteriaDto
import io.github.luksal.integration.source.openlibrary.api.dto.OpenLibraryDoc
import io.github.luksal.util.ext.logger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service


@Service
class BookService(
    private val bookJpaRepository: BookJpaRepository,
    private val bookBasicInfoDocumentRepository: BookBasicInfoDocumentRepository,
    private val bookDocumentRepository: BookDocumentRepository
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

    fun saveBookDocuments(books: List<Book>) {
        if (books.isEmpty()) {
            log.info("No books to save")
            return
        }
        bulkSaveNoDuplicatesBooks(books)
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
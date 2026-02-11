package io.github.luksal.book.service

import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.db.document.BookBasicInfoDocumentRepository
import io.github.luksal.book.db.document.BookDocumentRepository
import io.github.luksal.book.db.document.model.BookBasicInfoDocument
import io.github.luksal.book.db.jpa.BookJpaRepository
import io.github.luksal.book.ext.logger
import io.github.luksal.book.ext.normalize
import io.github.luksal.book.ext.sha256
import io.github.luksal.book.model.Book
import io.github.luksal.book.model.toDocument
import io.github.luksal.book.openlibrary.api.dto.OpenLibraryDoc
import io.github.luksal.book.service.dto.BookSearchCriteriaDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Service
class BookService(
    private val bookJpaRepository: BookJpaRepository,
    private val bookBasicInfoDocumentRepository: BookBasicInfoDocumentRepository,
    private val bookDocumentRepository: BookDocumentRepository
) {

    private val log = logger()

    fun searchBooks(criteria: BookSearchCriteriaDto, pageable: Pageable): Page<BookSearchResponse> {
        return bookJpaRepository.searchAll(
            title = criteria.title,
            startYear = criteria.publishedYearRange.first,
            endYear = criteria.publishedYearRange.last,
            genres = criteria.genres.map { it.name }.toList(),
            pageable = pageable
        ).map { BookSearchResponse.fromEntity(book = it) }
    }

    fun getBookById(id: Long): BookSearchResponse {
        return bookJpaRepository.findById(id).map { BookSearchResponse.fromEntity(book = it) }
            .orElseThrow()
    }

    fun saveBooks(books: List<Book>) {
        bookDocumentRepository.saveAll(books.map { it.toDocument() })
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveBookBasicInfo(bookBasicInfo: List<OpenLibraryDoc> = emptyList(), lang: String): Int {
        return bookBasicInfo.map {
            BookBasicInfoDocument(
                id = (it.key + it.editionTitle()).normalize().sha256(),
                title = it.title,
                publicId = Uuid.generateV7().toString(),
                key = it.key,
                editionTitle = it.editionTitle(),
                editionKey = it.editionKey(),
                authors = it.authorName ?: emptyList(),
                firstPublishYear = it.firstPublishYear,
                lang = lang
            )
        }.let {
            bookBasicInfoDocumentRepository.saveAll(it).size
        }
    }

    fun getBookBasicInfo(page: Pageable): Page<BookBasicInfoDocument> {
        return bookBasicInfoDocumentRepository.findAll(page)
    }
}
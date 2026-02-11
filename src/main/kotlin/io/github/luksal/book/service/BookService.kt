package io.github.luksal.book.service

import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.db.document.BookBasicInfoDocumentRepository
import io.github.luksal.book.db.document.BookDocumentRepository
import io.github.luksal.book.db.document.model.BookBasicInfoDocument
import io.github.luksal.book.db.jpa.BookJpaRepository
import io.github.luksal.util.ext.logger
import io.github.luksal.util.ext.normalize
import io.github.luksal.util.ext.sha256
import io.github.luksal.book.model.Book
import io.github.luksal.book.model.toDocument
import io.github.luksal.ingestion.source.openlibrary.api.dto.OpenLibraryDoc
import io.github.luksal.book.service.dto.BookSearchCriteriaDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.FindAndReplaceOptions
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@Service
class BookService(
    private val bookJpaRepository: BookJpaRepository,
    private val bookBasicInfoDocumentRepository: BookBasicInfoDocumentRepository,
    private val bookDocumentRepository: BookDocumentRepository,
    private val mongoOps: MongoOperations
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
        //consider changing to upsert
        val duplicateIds = bookDocumentRepository.findAllById(books.map { it.publicId }).map { it.id }.toHashSet()
        bookDocumentRepository.saveAll(books.filterNot { duplicateIds.contains(it.publicId) }.map { it.toDocument() })
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveBookBasicInfo(bookBasicInfo: List<OpenLibraryDoc> = emptyList(), lang: String): Int {
        return bookBasicInfo.map {
            BookBasicInfoDocument(
                id = (it.title + it.editionTitle()).normalize().sha256(),
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
            val bulkOps = mongoOps.bulkOps(BulkOperations.BulkMode.UNORDERED, BookBasicInfoDocument::class.java)

            it.forEach { book ->
                bulkOps.replaceOne(
                    Query.query(Criteria.where("_id").`is`(book.id)),
                    book,
                    FindAndReplaceOptions.options().upsert()
                )
            }

            bulkOps.execute().upserts.size

            /*val duplicateIds = bookBasicInfoDocumentRepository.findAllById(it.map { details -> details.id }).map { record -> record.id }.toHashSet()
            bookBasicInfoDocumentRepository.saveAll(it.filterNot { details ->  duplicateIds.contains(details.id) }).size*/
        }
    }

    fun updateBookBasicInfo(bookBasicInfoDocument: List<BookBasicInfoDocument>) {
        bookBasicInfoDocumentRepository.saveAll(bookBasicInfoDocument)
    }

    fun getUnprocessedBookBasicInfo(page: Pageable): Page<BookBasicInfoDocument> =
        bookBasicInfoDocumentRepository.findByProcessed(false, page)

}
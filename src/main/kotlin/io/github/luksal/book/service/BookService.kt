package io.github.luksal.book.service

import io.github.luksal.book.api.dto.BookBasicInfoDetailsDto
import io.github.luksal.book.api.dto.BookBasicInfoDto
import io.github.luksal.book.api.dto.BookBasicInfoSearchCriteria
import io.github.luksal.book.api.dto.BookDetailsDto
import io.github.luksal.book.api.dto.BookDto
import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.db.document.author.AuthorDocument
import io.github.luksal.book.db.document.book.BookDocument
import io.github.luksal.book.db.document.book.repository.BookDocumentRepository
import io.github.luksal.book.db.document.bookbasicinfo.BookBasicInfoDocument
import io.github.luksal.book.db.document.bookbasicinfo.repository.BookBasicInfoDocumentRepository
import io.github.luksal.book.db.jpa.AuthorJpaRepository
import io.github.luksal.book.db.jpa.BookJpaRepository
import io.github.luksal.book.db.jpa.GenreJpaRepository
import io.github.luksal.book.db.jpa.RatingSourceJpaRepository
import io.github.luksal.book.db.jpa.model.BookEntity
import io.github.luksal.book.db.jpa.model.GenreEntity
import io.github.luksal.book.mapper.BookMapper
import io.github.luksal.book.mapper.BookMapper.mapToEntity
import io.github.luksal.book.mapper.BookMapper.toDetailsDto
import io.github.luksal.book.mapper.BookMapper.toDto
import io.github.luksal.book.model.*
import io.github.luksal.book.service.dto.BookSearchCriteriaDto
import io.github.luksal.integration.source.openlibrary.api.dto.OpenLibraryDoc
import io.github.luksal.util.ext.logger
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service


@Service
class BookService(
    private val bookJpaRepository: BookJpaRepository,
    private val authorJpaRepository: AuthorJpaRepository,
    private val genreJpaRepository: GenreJpaRepository,
    private val ratingSourceJpaRepository: RatingSourceJpaRepository,
    private val bookBasicInfoDocumentRepository: BookBasicInfoDocumentRepository,
    private val bookDocumentRepository: BookDocumentRepository,
    private val publisher: ApplicationEventPublisher
) {

    private val log = logger()

    fun searchBookDocuments(criteria: BookSearchCriteriaDto, pageable: Pageable): Page<BookSearchResponse> {
        return bookDocumentRepository.search(
            title = criteria.title,
            startYear = criteria.startYear,
            endYear = criteria.endYear,
            genres = criteria.genres?.map { it.name },
            pageable = pageable
        ).map { BookMapper.map(it) }
    }

    fun searchBookBasicInfo(criteria: BookBasicInfoSearchCriteria, pageable: Pageable): Page<BookBasicInfoDto> {
        return bookBasicInfoDocumentRepository.search(
            id = criteria.id,
            bookId = criteria.bookId,
            title = criteria.title,
            startYear = criteria.startYear,
            endYear = criteria.endYear,
            pageable = pageable
        ).map { BookMapper.map(it) }
    }

    fun getBooksByIds(bookPublicIds: List<String>): List<BookSearchResponse> =
        bookJpaRepository.findAllById(bookPublicIds).map { BookMapper.map(it) }

    //TODO think more of this failover strategy
    fun getBookByIdForCrawling(id: String): BookSearchResponse =
            bookDocumentRepository.findById(id).orElseThrow()
            .let { BookMapper.map(it) }


    fun getBookById(id: String): BookDetailsDto =
        bookJpaRepository.findById(id)
            .orElseThrow()
            .toDetailsDto()

    fun searchBooks(criteria: BookSearchCriteriaDto, pageable: Pageable): Page<BookDto> {
        return bookJpaRepository.search(
            title = criteria.title,
            startYear = criteria.startYear,
            endYear = criteria.endYear,
            genres =  criteria.genres?.takeIf { it.isNotEmpty() }?.map { it.name },
            pageable = pageable
        ).map { it.toDto() }
    }

    fun getBookDocumentByIds(ids: List<String>): List<BookDocument> =
        bookDocumentRepository.findAllById(ids)

    fun getBookBasicInfoById(bookId: String): BookBasicInfoDetailsDto? =
        bookBasicInfoDocumentRepository.findById(bookId).map {
            BookMapper.mapDetails(it)
        }.orElse(null)

    @Transactional
    fun saveBookDocuments(books: List<Book>) {
        if (books.isEmpty()) {
            log.info("No book documents to save")
            return
        }
        bulkSaveNoDuplicatesBooks(books)
        books.forEach { event ->
            publisher.publishEvent(BookDocumentSavedEvent(bookId = event.id))
        }
    }

    fun saveBookEntities(books: List<BookEntity>) {
        if (books.isEmpty()) {
            log.info("No book entities to save")
            return
        }
        bookJpaRepository.saveAll(books)
        books.forEach {
            publisher.publishEvent(BookEntitySavedEvent(bookId = it.bookId!!))
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

            bulkSaveNoDuplicatesBasicBookInfo(bookBasicInfo).also {
                bookBasicInfo.forEach {
                    publisher.publishEvent(BookBasicInfoDocumentSavedEvent(bookId = it.bookPublicId))
                }
            }
        }

    @Transactional
    fun saveBookBasicInfo(bookBasicInfo: List<OpenLibraryDoc>, lang: String): Int =
        bookBasicInfo.map { it.toBasicInfoDocument(lang) }.let { bookBasicInfo ->
            if (bookBasicInfo.isEmpty()) {
                log.info("No book basic info to save for lang=$lang")
                return@let 0
            }

            bulkSaveNoDuplicatesBasicBookInfo(bookBasicInfo).also {
                bookBasicInfo.forEach {
                    publisher.publishEvent(BookBasicInfoDocumentSavedEvent(bookId = it.bookPublicId))
                }
            }
        }

    @Transactional
    fun syncToEntity(document: BookDocument) {
        try {
            val authors = document.authors?.map { author ->
                authorJpaRepository.findByPublicId(AuthorDocument.generatePublicId(author.key, author.name))
                    ?: authorJpaRepository.saveAndFlush(BookMapper.mapAuthorToEntity(author))
            }?.toMutableSet() ?: mutableSetOf()

            val genres = document.genres?.map { genre ->
                genreJpaRepository.findByName(genre.name)
                    ?: genreJpaRepository.saveAndFlush(GenreEntity(name = genre.name))
            }?.toMutableSet() ?: mutableSetOf()

            val bookEntity : BookEntity = bookJpaRepository.findById(document.id)
                .orElse(document.mapToEntity(authors, genres))!!

            bookEntity.ratings.clear()
            bookEntity.ratings.addAll(document.ratings?.map {
                val source = ratingSourceJpaRepository.findByName(it.source.name)
                    ?: ratingSourceJpaRepository.save(it.source.mapToEntity())
                it.mapToEntity(bookEntity, source)
            }?.toMutableList() ?: mutableListOf())
            try {
                bookJpaRepository.saveAndFlush(bookEntity)
            } catch (ex: DataIntegrityViolationException) {
                log.warn("Book document already present ${document.title} ")
            }
        } catch (e: DataIntegrityViolationException) {
            log.warn("Failed to sync book ${document.id}", e)
        }
    }

    fun updateBookBasicInfo(bookBasicInfoDocument: List<BookBasicInfoDocument>) =
        bookBasicInfoDocumentRepository.saveAll(bookBasicInfoDocument)


    fun getBookBasicInfo(bookIds: List<String>, page: Pageable): Page<BookBasicInfoDocument> =
        bookBasicInfoDocumentRepository.findAllByBookPublicIdIn(bookIds, page)

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
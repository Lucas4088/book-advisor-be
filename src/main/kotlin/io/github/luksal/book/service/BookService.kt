package io.github.luksal.book.service

import com.github.pemistahl.lingua.api.Language
import io.github.luksal.book.api.dto.*
import io.github.luksal.book.db.document.author.AuthorDocument
import io.github.luksal.book.db.document.book.BookDocument
import io.github.luksal.book.db.document.book.repository.BookDocumentRepository
import io.github.luksal.book.db.document.bookbasicinfo.BookBasicInfoDocument
import io.github.luksal.book.db.document.bookbasicinfo.repository.BookBasicInfoDocumentRepository
import io.github.luksal.book.db.jpa.*
import io.github.luksal.book.db.jpa.model.BookEntity
import io.github.luksal.book.db.jpa.model.GenreEntity
import io.github.luksal.book.db.jpa.model.RatingEntity
import io.github.luksal.book.mapper.BookMapper
import io.github.luksal.book.mapper.BookMapper.mapToEditionEntity
import io.github.luksal.book.mapper.BookMapper.mapToEntity
import io.github.luksal.book.mapper.BookMapper.mapToSearchResponse
import io.github.luksal.book.mapper.BookMapper.toDetailsDto
import io.github.luksal.book.mapper.BookMapper.toDocument
import io.github.luksal.book.mapper.BookMapper.toDto
import io.github.luksal.book.model.*
import io.github.luksal.book.service.dto.BookSearchCriteriaDto
import io.github.luksal.event.service.EventService
import io.github.luksal.integration.source.openlibrary.api.dto.OpenLibraryDoc
import io.github.luksal.util.ext.logger
import jakarta.transaction.Transactional
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode


@Service
class BookService(
    private val bookJpaRepository: BookJpaRepository,
    private val bookEditionJpaRepository: BookEditionJpaRepository,
    private val authorJpaRepository: AuthorJpaRepository,
    private val genreJpaRepository: GenreJpaRepository,
    private val ratingSourceJpaRepository: RatingSourceJpaRepository,
    private val bookBasicInfoDocumentRepository: BookBasicInfoDocumentRepository,
    private val bookDocumentRepository: BookDocumentRepository,
    private val eventService: EventService
) {

    companion object {
        private val log = logger()
        val EDITION_IMPORT_LANGUAGES = listOf(Language.POLISH, Language.ENGLISH)
    }

    fun searchBookBasicInfo(criteria: BookBasicInfoSearchCriteria, pageable: Pageable): Page<BookBasicInfoDto> {
        return bookBasicInfoDocumentRepository.search(
            id = criteria.id,
            bookId = criteria.bookId,
            title = criteria.title,
            startYear = criteria.startYear,
            endYear = criteria.endYear,
            pageable = pageable
        ).map { it.toDto() }
    }

    //TODO think more of this failover strategy
    fun getBookByIdForCrawling(id: String): BookSearchResponse =
        bookDocumentRepository.findById(id).orElseThrow().mapToSearchResponse()

    fun getBookById(id: String): BookDetailsDto {
        val book = bookJpaRepository.findById(id)
            .orElseThrow()
        val ratingResult = createTotalRating(book.ratings)
        return book
            .toDetailsDto(ratingResult)
    }


    fun searchBooks(criteria: BookSearchCriteriaDto, pageable: Pageable): Page<BookDto> {
        return bookJpaRepository.search(
            title = criteria.title?.lowercase(),
            startYear = criteria.startYear,
            endYear = criteria.endYear,
            genres = criteria.genres?.takeIf { it.isNotEmpty() }?.map { it.name },
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
        books.forEach { book ->
            eventService.publish(BookDocumentSavedEvent(bookId = book.id))
        }
    }

    fun saveBookEntities(books: List<BookEntity>) {
        if (books.isEmpty()) {
            log.info("No book entities to save")
            return
        }
        bookJpaRepository.saveAll(books)
        books.forEach {
            eventService.publish(BookEntitySavedEvent(bookId = it.bookId!!))
        }
    }

    fun updateBook(bookUpdate: BookUpdate): String? =
        bookDocumentRepository.update(bookUpdate)

    @Transactional
    fun saveBookBasicInfo(bookBasicInfo: List<BookBasicInfoDocument>): Int =
        saveBookBasicInfoBulk(bookBasicInfo).also {
            bookBasicInfo.forEach {
                eventService.publish(BookBasicInfoDocumentSavedEvent(bookId = it.bookPublicId))
            }
        }

    @Transactional
    fun saveBookBasicEdition(bookBasicInfo: List<BookBasicInfoDocument>): Int =
        saveBookBasicInfoBulk(bookBasicInfo).also {
            bookBasicInfo.forEach {
                eventService.publish(BookBasicInfoDocumentEditionSavedEvent(bookId = it.bookPublicId))
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
                    eventService.publish(BookBasicInfoDocumentSavedEvent(bookId = it.bookPublicId))
                }
            }
        }

    @Transactional
    fun syncToEntity(document: BookDocument) {
        syncBook(document)
        eventService.publish(BookEntitySavedEvent(bookId = document.id))
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

    private fun syncBook(document: BookDocument) {
        try {
            if (document.language == null) {
                return
            }
            if (document.isEdition) {
                syncBookEdition(document)
                return
            }

            val authors = document.authors?.map { author ->
                authorJpaRepository.findByPublicId(AuthorDocument.generatePublicId(author.key, author.name))
                    ?: authorJpaRepository.saveAndFlush(BookMapper.mapAuthorToEntity(author))
            }?.toMutableSet() ?: mutableSetOf()

            val genres = document.genres?.map { genre ->
                genreJpaRepository.findByName(genre.name)
                    ?: genreJpaRepository.saveAndFlush(GenreEntity(name = genre.name))
            }?.toMutableSet() ?: mutableSetOf()

            val bookEntity: BookEntity = bookJpaRepository.findById(document.id)
                .orElse(document.mapToEntity(authors, genres))!!

            bookEntity.ratings.clear()
            bookEntity.ratings.addAll(document.ratings?.map {
                val source = ratingSourceJpaRepository.findByName(it.source.name)
                    ?: ratingSourceJpaRepository.save(it.source.mapToEntity())
                it.mapToEntity(bookEntity, source)
            }?.toMutableList() ?: mutableListOf())
            try {
                bookJpaRepository.saveAndFlush(bookEntity)
                eventService.publish(BookEntitySavedEvent(bookId = document.id))
            } catch (ex: DataIntegrityViolationException) {
                log.warn("Book document already present ${document.title} ")
            }
        } catch (e: DataIntegrityViolationException) {
            log.warn("Failed to sync book ${document.id}", e)
        }
    }

    private fun syncBookEdition(editionDocument: BookDocument) {
        val bookDocument = getBookDocumentByKey(editionDocument.openLibraryKey)
            ?: throw NoSuchElementException("No book document found for key ${editionDocument.openLibraryKey}")
        val bookEntity: BookEntity = bookJpaRepository.findById(bookDocument.id)
            .orElseThrow()
        bookEditionJpaRepository.save(editionDocument.mapToEditionEntity(bookEntity))

    }

    private fun bulkSaveNoDuplicatesBooks(books: List<Book>) =
        bookDocumentRepository.saveBulkWithDeduplication(books.map { it.toDocument() })

    private fun saveBookBasicInfoBulk(bookBasicInfo: List<BookBasicInfoDocument>): Int =
        bookBasicInfo.let { bookBasicInfo ->
            if (bookBasicInfo.isEmpty()) {
                log.info("No book basic info to save")
                return@let 0
            }
            bulkSaveNoDuplicatesBasicBookInfo(bookBasicInfo)
        }

    private fun bulkSaveNoDuplicatesBasicBookInfo(documents: List<BookBasicInfoDocument>): Int =
        bookBasicInfoDocumentRepository.saveBulkWithDeduplication(documents).size

    private fun getBookDocumentByKey(key: String): BookDocument? =
        bookDocumentRepository.findByKey(key)

    private fun createTotalRating(ratings: List<RatingEntity>): RatingResult {
        val ratingsSources = ratings.map { RatingSingleSource(it.score, it.count, it.source.name) }
        val totalRatingCount = ratings.sumOf { it.count ?: 1 }.takeIf { it != 0 } ?: 1
        val averageRatingScore = ratings.fold(BigDecimal.ZERO) { acc, entity ->
            acc.add(
                entity.score.multiply(
                    entity.count?.toBigDecimal() ?: BigDecimal.ONE
                )
            )
        }.divide(totalRatingCount.toBigDecimal(), 2, RoundingMode.HALF_EVEN)
        return RatingResult(
            averageRatingScore,
            totalRatingCount,
            ratingsSources
        )
    }


}
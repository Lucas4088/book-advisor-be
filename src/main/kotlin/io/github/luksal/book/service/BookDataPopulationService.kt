package io.github.luksal.book.service

import com.github.pemistahl.lingua.api.LanguageDetector
import io.github.luksal.book.db.document.bookbasicinfo.BookBasicInfoDocument
import io.github.luksal.book.db.jpa.event.PopulateBookBasicDataJpaRepository
import io.github.luksal.book.db.jpa.event.PopulateBookDetailsEventJpaRepository
import io.github.luksal.book.db.jpa.model.event.ScheduledBookBasicInfoPopulationEventEntity
import io.github.luksal.book.mapper.BookMapper.toModel
import io.github.luksal.book.model.Book
import io.github.luksal.commons.dto.EventStatus
import io.github.luksal.commons.jpa.EventMeta
import io.github.luksal.event.service.EventService
import io.github.luksal.ingestion.api.dto.ScheduledBookBasicInfoPopulationEvent
import io.github.luksal.ingestion.api.dto.ScheduledBookBasicInfoSearchRequest
import io.github.luksal.ingestion.mapper.IngestionMapper
import io.github.luksal.integration.event.listener.BookDetailsFetchedEvent
import io.github.luksal.integration.source.archivebooks.ArchiveBooksService
import io.github.luksal.integration.source.archivebooks.api.dto.ArchiveSearchDoc
import io.github.luksal.integration.source.googlebooks.GoogleBooksService
import io.github.luksal.integration.source.googlebooks.api.dto.BookItem
import io.github.luksal.integration.source.openlibrary.OpenLibraryService
import io.github.luksal.integration.source.openlibrary.api.dto.OpenLibraryBookDetails
import io.github.luksal.integration.source.openlibrary.api.dto.OpenLibraryDoc
import io.github.luksal.mail.EmailService
import io.github.luksal.util.ext.levenshteinDistance
import io.github.luksal.util.ext.logger
import io.github.luksal.util.ext.normalizeStandardChars
import jakarta.transaction.Transactional
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper

@Service
class BookDataPopulationService(
    private val bookService: BookService,
    private val bookBasicDataPopulationJpaRepository: PopulateBookBasicDataJpaRepository,
    private val populateBookDetailsEventJpaRepository: PopulateBookDetailsEventJpaRepository,
    private val openLibraryService: OpenLibraryService,
    private val googleBooksService: GoogleBooksService,
    private val archiveBooksService: ArchiveBooksService,
    private val redisTemplate: RedisTemplate<Any, Any>,
    private val emailService: EmailService,
    private val eventService: EventService,
) {

    private val log = logger()

    @Transactional
    fun searchBasicBookInfoSchedule(
        request: ScheduledBookBasicInfoSearchRequest,
        page: Pageable
    ): Page<ScheduledBookBasicInfoPopulationEvent> =
        bookBasicDataPopulationJpaRepository.searchAll(
            request.fromYear, request.toYear, request.lang, request.status, page
        ).map { IngestionMapper.map(it) }

    fun scheduleBasicBookInfoCollection(fromYear: Int, toYear: Int, lang: String) {
        (fromYear..toYear).filter {
            !bookBasicDataPopulationJpaRepository.existsByYearAndLangAndMeta_Status(it, lang, EventStatus.PENDING)
        }.map {
            ScheduledBookBasicInfoPopulationEventEntity(
                year = it,
                lang = lang,
                meta = EventMeta(status = EventStatus.PENDING),
            )
        }.let { bookBasicDataPopulationJpaRepository.saveAll(it) }
    }


    @Transactional
    fun populateBasicBookInfoCollection() {
        log.info("Starting book basic info collection initialization")
        val limit = 5000
        var scheduled = bookBasicDataPopulationJpaRepository.findFirstByMeta_Status(EventStatus.PENDING) ?: run {
            log.info("No scheduled tasks found for book basic info collection initialization, exiting")
            return
        }
        val fromYear = scheduled.year
        var savedCount = 0
        runCatching {
            val response = openLibraryService.searchBy(
                scheduled.year, scheduled.year,
                scheduled.lang, 1, limit
            )
            savedCount = response.docs.size
            bookService.saveBookBasicInfo(response.docs, scheduled.lang)
            scheduled.apply { meta.markAsSuccess() }
        }.onFailure { e ->
            log.error("Error during initialization: ${e.message}", e)
            scheduled.apply {
                meta.markAsFailed(e.message ?: "Unknown error")
            }
            sendBasicBookInfoErrorNotificationEmail(
                scheduled.year,
                scheduled.lang,
                e.message ?: "Unknown error"
            )
        }
        bookBasicDataPopulationJpaRepository.save(scheduled)
        scheduled = bookBasicDataPopulationJpaRepository.findFirstByMeta_Status(EventStatus.PENDING) ?: return
        //TODO move to jobs otherwise it will be a spam
        sendBasicBookInfoSuccessNotificationEmail(fromYear, scheduled.year, scheduled.lang, savedCount)
        log.info("Book basic info collection initialization completed")
    }

    @Transactional
    fun populateBooksCollection() {
        log.info("Starting book details collection initialization")
        val pageNumber = 0
        val pageSize = 20
        val populateEventMap =
            populateBookDetailsEventJpaRepository.findAllPending(PageRequest.of(pageNumber, pageSize)).content
                .associateBy { it.bookId }
        if (populateEventMap.isEmpty()) {
            log.info("No pending events found for book details collection initialization, exiting")
            return
        }
        val bookBasicInfo = bookService.getBookBasicInfo(
            populateEventMap.keys.toList(),
            PageRequest.of(pageNumber, pageSize)
        )

        //TODO handle this case, it shouldn't be like this, there is some inconsistency in the data
        if (!populateEventMap.isEmpty() && bookBasicInfo.content.isEmpty()) {
            populateEventMap.values.forEach {
                it.meta.markAsSkipped()
            }
            return
        }

        bookBasicInfo.content.mapNotNull { bookInfo ->
            val populateEvent = populateEventMap[bookInfo.bookPublicId]
            runCatching {
                if(bookInfo.lang == null
                    || bookInfo.isEdition && bookInfo.lang != "POLISH"
                    || !BookService.EDITION_IMPORT_LANGUAGES.map { it.name }.contains(bookInfo.lang)) {
                    populateEvent?.meta?.markAsSkipped()
                    return@mapNotNull null
                }
                findBookDetails(bookInfo)
            }.onSuccess { book ->
                if (book != null) {
                    populateEvent?.meta?.markAsSuccess()
                } else {
                    populateEvent?.meta?.markAsSkipped()
                    log.warn(
                        "No details found for book with title='${bookInfo.title}' and authors='${
                            bookInfo.authors.joinToString(
                                ","
                            )
                        }'"
                    )
                }
            }.onFailure { e ->
                populateEvent?.meta?.markAsFailed(e.message ?: "Unknown error")
                log.error(
                    "Error processing book with title='${bookInfo.title}' and authors='${
                        bookInfo.authors.joinToString(
                            ","
                        )
                    }': ${e.message}", e
                )
            }.getOrNull()
        }.let {
            bookService.saveBookDocuments(it)
        }
        populateBookDetailsEventJpaRepository.saveAll(populateEventMap.values)
        log.info("Book details collection initialization completed")
    }

    private fun findBookDetails(bookInfo: BookBasicInfoDocument): Book? {
        val response = redisTemplate.opsForValue()["google-books:${bookInfo.id}"] as BookItem?
            ?: googleBooksService.findBookDetails(bookInfo.title, bookInfo.authors)?.items
                ?.firstOrNull()
                ?.also { redisTemplate.opsForValue()["google-books:${bookInfo.id}"] = it }

        return response
            ?.also { publishEvent("GOOGLE_BOOKS", it) }
            ?.toModel(bookInfo, bookInfo.lang)
            ?: fetchFallbackBookDetails(bookInfo)
    }

    private fun fetchFallbackBookDetails(unprocessed: BookBasicInfoDocument): Book? {
        val archiveBookDetailsResponse = resolveArchiveBookDetails(unprocessed.title, unprocessed.authors)

        val pair = fetchOpenLibraryBookInfo(unprocessed.title, unprocessed.authors, unprocessed.openLibraryKey)
        val openLibraryDoc = pair.first
        val openLibraryBookDetails = pair.second
        if (openLibraryDoc == null && archiveBookDetailsResponse == null) {
            log.warn(
                "No fallback details found for book with title='${unprocessed.title}' and authors='${
                    unprocessed.authors.joinToString(
                        ","
                    )
                }'"
            )
            return null
        }

        return unprocessed.toModel(
            openLibraryBookDetails,
            openLibraryDoc,
            archiveBookDetailsResponse,
            unprocessed.lang
        )
    }

    private fun fetchOpenLibraryBookInfo(
        title: String, authors: List<String>, openLibraryKey: String?
    ): Pair<OpenLibraryDoc?, OpenLibraryBookDetails?> {
        val search = openLibraryService.searchBy(
            title,
            authors.firstOrNull() ?: ""
        )?.docs?.firstOrNull()

        val details = openLibraryKey
            ?.let { openLibraryService.getBookDetails(it) }
            .also { publishEvent("OPEN_LIBRARY", it) }
        return Pair(search, details)
    }

    private fun resolveArchiveBookDetails(
        title: String, authors: List<String>
    ): ArchiveSearchDoc? {
        val archiveBookResponse = archiveBooksService.search(title, authors.firstOrNull())
        return archiveBookResponse.minWithOrNull(
            compareBy<ArchiveSearchDoc> { response ->
                response.title?.levenshteinDistance(title)
            }
                .thenByDescending { response ->
                    response.description?.maxOfOrNull { it.normalizeStandardChars().length } ?: 0
                }
                .thenByDescending { response -> response.collection?.size ?: 0 }
        )?.also { log.info(ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(it)) }
            .also { publishEvent("ARCHIVE_BOOKS", it) }
    }

    private fun sendBasicBookInfoSuccessNotificationEmail(fromYear: Int, toYear: Int, lang: String, savedCount: Int) =
        CoroutineScope(Dispatchers.IO).launch {
            log.info("Sending success notification email for book basic info collection initialization")
            emailService.sendEmail(
                "lukasz4088@gmail.com", "Book basic info collection initialization completed",
                """
                    The initialization of the book basic info collection for years $fromYear-$toYear and language $lang has been completed.
                    Total saved records: $savedCount.
                    """.trimIndent()
            )
        }

    //TODO add email for empty processing

    private fun sendBasicBookInfoErrorNotificationEmail(
        year: Int,
        lang: String,
        errorMessage: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        log.error("Sending error notification email for book basic info collection initialization failure")
        emailService.sendEmail(
            "lukasz4088@gmail.com", "Book basic info collection initialization failed",
            """
                    The initialization of the book basic info collection for year $year and language $lang has failed.
                    Error message: $errorMessage
                    """.trimIndent()
        )
    }

    private fun publishEvent(sourceName: String, response: Any?) {
        val status = if (response == null) EventStatus.ERROR else EventStatus.SUCCESS
        eventService.publishAndEmit(sourceName, BookDetailsFetchedEvent(sourceName, status))
    }
}
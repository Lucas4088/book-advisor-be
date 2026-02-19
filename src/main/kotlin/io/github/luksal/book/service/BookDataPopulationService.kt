package io.github.luksal.book.service

import io.github.luksal.book.common.jpa.event.EventMeta
import io.github.luksal.book.common.jpa.event.EventStatus
import io.github.luksal.book.db.document.bookbasicinfo.BookBasicInfoDocument
import io.github.luksal.book.db.jpa.event.PopulateBookBasicDataJpaRepository
import io.github.luksal.book.db.jpa.event.PopulateBookDetailsEventJpaRepository
import io.github.luksal.book.db.jpa.model.event.ScheduledBookBasicInfoPopulationEventEntity
import io.github.luksal.book.mapper.BookMapper
import io.github.luksal.book.model.Book
import io.github.luksal.integration.source.archivebooks.ArchiveBooksService
import io.github.luksal.integration.source.archivebooks.api.dto.ArchiveSearchDoc
import io.github.luksal.integration.source.googlebooks.GoogleBooksService
import io.github.luksal.integration.source.openlibrary.OpenLibraryService
import io.github.luksal.integration.source.openlibrary.api.dto.OpenLibraryBookDetails
import io.github.luksal.integration.source.openlibrary.api.dto.OpenLibraryDoc
import io.github.luksal.mail.EmailService
import io.github.luksal.util.ext.levenshteinDistance
import io.github.luksal.util.ext.logger
import io.github.luksal.util.ext.normalizeStandardChars
import jakarta.transaction.Transactional
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.data.domain.PageRequest
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
    private val customInitializerDispatcher: CoroutineDispatcher,
    private val emailService: EmailService
) {

    private val log = logger()

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

    fun populateBasicBookInfoCollection() {
        CoroutineScope(customInitializerDispatcher).launch {
            log.info("Starting book basic info collection initialization")

            val limit = 5000
            var totalSavedCount = 0
            var scheduled = bookBasicDataPopulationJpaRepository.findFirstByMeta_Status(EventStatus.PENDING) ?: run {
                sendBasicBookInfoSuccessNotificationEmail(0, 0, "", totalSavedCount)
                log.info("No scheduled tasks found for book basic info collection initialization, exiting")
                return@launch
            }
            val fromYear = scheduled.year
            while (true) {
                var savedCount = 0
                try {
                    var page = 0
                    do {
                        page++
                        val response = openLibraryService.searchBy(
                            scheduled.year, scheduled.year,
                            scheduled.lang, page, limit
                        )
                        bookService.saveBookBasicInfo(response.docs, scheduled.lang)

                        savedCount += response.docs.size
                        totalSavedCount += savedCount

                    } while (response.docs.isNotEmpty())
                    scheduled.apply {
                        meta.status = EventStatus.SUCCESS
                        meta.updatedAt = System.currentTimeMillis()
                    }
                } catch (e: Exception) {
                    log.error("Error during initialization: ${e.message}", e)
                    scheduled.apply {
                        meta.status = EventStatus.ERROR
                        meta.updatedAt = System.currentTimeMillis()
                        meta.errorMessage = e.message
                    }
                    sendBasicBookInfoErrorNotificationEmail(
                        scheduled.year,
                        scheduled.lang,
                        savedCount,
                        e.message ?: "Unknown error"
                    )
                }
                bookBasicDataPopulationJpaRepository.save(scheduled)
                scheduled = bookBasicDataPopulationJpaRepository.findFirstByMeta_Status(EventStatus.PENDING) ?: break
            }
            sendBasicBookInfoSuccessNotificationEmail(fromYear, scheduled.year, scheduled.lang, totalSavedCount)
            log.info("Book basic info collection initialization completed")
        }
    }

    //TODO check if tx works
    @Transactional
    fun populateBooksCollection() {
        CoroutineScope(customInitializerDispatcher).launch {
            log.info("Starting book details collection initialization")
            var pageNumber = 0
            val pageSize = 20
            do {
                val populateEventMap = populateBookDetailsEventJpaRepository.findAllPending(PageRequest.of(pageNumber, pageSize)).content
                        .associateBy { it.bookId }
                if(populateEventMap.isEmpty()) {
                    log.info("No pending events found for book details collection initialization, exiting")
                    break
                }
                val unprocessedTitles = bookService.getUnprocessedBookBasicInfo(
                    populateEventMap.values.map { it.bookId },
                    PageRequest.of(pageNumber, pageSize)
                )
                unprocessedTitles.content.mapNotNull { bookInfo ->
                    val populateEvent = populateEventMap[bookInfo.publicId]
                    runCatching {
                        findBookDetails(bookInfo)
                    }.onSuccess { book ->
                        if (book != null) {
                            populateEvent?.meta?.markAsSuccess()
                        } else {
                            populateEvent?.meta?.markAsSkipped()
                            log.warn("No details found for book with title='${bookInfo.title}' and authors='${bookInfo.authors.joinToString(",")}'")
                        }
                    }.onFailure { e ->
                        populateEvent?.meta?.markAsFailed(e.message ?: "Unknown error")
                        log.error("Error processing book with title='${bookInfo.title}' and authors='${bookInfo.authors.joinToString(",")}': ${e.message}", e)
                    }.getOrNull()
                }.let {
                    bookService.saveBookDocuments(it)
                    populateBookDetailsEventJpaRepository.saveAll(populateEventMap.values)
                }
                pageNumber++
            } while (!unprocessedTitles.isLast)
            log.info("Book details collection initialization completed")
        }
    }

    private fun findBookDetails(bookInfo: BookBasicInfoDocument): Book? =
        (googleBooksService.findBookDetails(bookInfo.title, bookInfo.authors)?.items
            ?.firstOrNull()
            ?.let { BookMapper.map(it, bookInfo) }
            ?: fetchFallbackBookDetails(bookInfo))

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
        return BookMapper.map(
            openLibraryBookDetails,
            openLibraryDoc,
            archiveBookDetailsResponse,
            unprocessed
        )
    }

    private fun fetchOpenLibraryBookInfo(
        title: String, authors: List<String>, openLibraryKey: String?
    ): Pair<OpenLibraryDoc?, OpenLibraryBookDetails?> {
        val search = openLibraryService.searchBy(
            title,
            authors.firstOrNull() ?: ""
        ).docs.firstOrNull()
        val details = openLibraryKey?.let {
            openLibraryService.getBookDetails(it)
        }
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
        savedCount: Int,
        errorMessage: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        log.error("Sending error notification email for book basic info collection initialization failure")
        emailService.sendEmail(
            "lukasz4088@gmail.com", "Book basic info collection initialization failed",
            """
                    The initialization of the book basic info collection for year $year and language $lang has failed.
                    Total saved records before failure: $savedCount.
                    Error message: $errorMessage
                    """.trimIndent()
        )
    }
}
package io.github.luksal.book.service

import io.github.luksal.book.db.jpa.BookBasicDataPopulationJpaRepository
import io.github.luksal.book.db.jpa.model.BookBasicDataPopulationScheduledYearEntity
import io.github.luksal.util.ext.logger
import io.github.luksal.ingestion.source.googlebooks.GoogleBooksService
import io.github.luksal.ingestion.source.googlebooks.api.dto.toBook
import io.github.luksal.mail.EmailService
import io.github.luksal.ingestion.source.openlibrary.OpenLibraryService
import kotlinx.coroutines.*
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class BookDataPopulationService(
    private val bookService: BookService,
    private val bookBasicDataPopulationJpaRepository: BookBasicDataPopulationJpaRepository,
    private val openLibraryService: OpenLibraryService,
    private val googleBooksService: GoogleBooksService,
    private val customInitializerDispatcher: CoroutineDispatcher,
    private val emailService: EmailService
) {

    private val log = logger()

    fun scheduleBasicBookInfoCollection(fromYear: Int, toYear: Int, lang: String) {
        (fromYear..toYear).filter {
            !bookBasicDataPopulationJpaRepository.existsByYearAndLangAndProcessed(it, lang, false)
        }.map {
            BookBasicDataPopulationScheduledYearEntity(
                year = it,
                lang = lang,
                processed = false,
                timestamp = System.currentTimeMillis()
            )
        }.let { bookBasicDataPopulationJpaRepository.saveAll(it) }
    }

    fun populateBasicBookInfoCollection() {
        CoroutineScope(customInitializerDispatcher).launch {
            log.info("Starting book basic info collection initialization")

            val limit = 5000
            var totalSavedCount = 0
            var scheduled = bookBasicDataPopulationJpaRepository.findFirstByProcessedIsFalse() ?: run {
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
                        val response = openLibraryService.searchBooks(
                            scheduled.year, scheduled.year,
                            scheduled.lang, page, limit
                        )
                        bookService.saveBookBasicInfo(response.docs, scheduled.lang)

                        savedCount += response.docs.size
                        totalSavedCount += savedCount

                    } while (response.docs.isNotEmpty())
                    bookBasicDataPopulationJpaRepository.save(scheduled.apply { processed = true })
                } catch (e: Exception) {
                    log.error("Error during initialization: ${e.message}", e)
                    sendBasicBookInfoErrorNotificationEmail(
                        scheduled.year,
                        scheduled.lang,
                        savedCount,
                        e.message ?: "Unknown error"
                    )
                }
                scheduled = bookBasicDataPopulationJpaRepository.findFirstByProcessedIsFalse() ?: break
            }
            sendBasicBookInfoSuccessNotificationEmail(fromYear, scheduled.year, scheduled.lang, totalSavedCount)
            log.info("Book basic info collection initialization completed")
        }
    }

    fun populateBooksCollection() {
        CoroutineScope(customInitializerDispatcher).launch {
            log.info("Starting book details collection initialization")
            var pageNumber = 0
            val pageSize = 50
            do {
                val unprocessedTitles = bookService.getUnprocessedBookBasicInfo(PageRequest.of(pageNumber, pageSize))

                unprocessedTitles.content.mapNotNull {
                    googleBooksService.findBookDetails(it.title, it.authors)?.items?.firstOrNull()?.toBook(it.publicId, it.editionTitle, it.lang)
                }.let {
                    bookService.saveBooks(it)
                }
                unprocessedTitles.forEach { info -> info.processed = true}
                bookService.updateBookBasicInfo(unprocessedTitles.toList())
                pageNumber++
            } while (!unprocessedTitles.isLast)
            log.info("Book details collection initialization completed")
        }
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
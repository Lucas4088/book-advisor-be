package io.github.luksal.book.service

import io.github.luksal.book.db.jpa.BookBasicDataPopulationJpaRepository
import io.github.luksal.book.db.jpa.model.BookBasicDataPopulationScheduledYearEntity
import io.github.luksal.book.ext.logger
import io.github.luksal.book.mail.EmailService
import io.github.luksal.book.openlibrary.api.OpenLibraryService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookDataPopulationService(
    private val bookService: BookService,
    private val bookBasicDataPopulationJpaRepository: BookBasicDataPopulationJpaRepository,
    private val openLibraryService: OpenLibraryService,
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

    suspend fun populateBasicBookInfoCollection() {
        CoroutineScope(customInitializerDispatcher).launch {
            log.info("Starting book basic info collection initialization")
            var page = 0
            val limit = 10000
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
                    do {
                        page++
                        val response =
                            openLibraryService.searchBooks(scheduled.year, scheduled.year, scheduled.lang, page, limit)
                        bookService.saveBookBasicInfo(response.docs, scheduled.lang)
                        savedCount += response.docs.size
                        totalSavedCount += savedCount
                        check(page != 3) { throw RuntimeException("Simulated error after 3 pages to test error handling")}
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
                    throw e
                }
                scheduled = bookBasicDataPopulationJpaRepository.findFirstByProcessedIsFalse() ?: break
            }
            sendBasicBookInfoSuccessNotificationEmail(fromYear, scheduled.year, scheduled.lang, totalSavedCount)
            log.info("Book basic info collection initialization completed")
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
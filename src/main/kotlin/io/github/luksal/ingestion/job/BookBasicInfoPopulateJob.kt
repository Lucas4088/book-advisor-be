package io.github.luksal.ingestion.job

import io.github.luksal.book.service.BookDataPopulationService
import org.springframework.scheduling.annotation.Scheduled

class BookBasicInfoPopulateJob(
    private val bookPopulateService: BookDataPopulationService
) {

    @Scheduled(fixedDelay = 1000)
    fun run() {
        while (true) {
            bookPopulateService.populateBasicBookInfoCollection()
        }
    }
}
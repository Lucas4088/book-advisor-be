package io.github.luksal.ingestion.job

import io.github.luksal.book.service.BookDataPopulationService
import io.github.luksal.ingestion.job.dto.JobName
import io.github.luksal.ingestion.jpa.JobRunPolicyRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class BookBasicInfoPopulateJob(
    private val bookPopulateService: BookDataPopulationService,
    private val jobRunPolicyRepository: JobRunPolicyRepository,
) {

    @Scheduled(fixedDelay = 1000)
    fun run() {
        val isEnabled = jobRunPolicyRepository.findByName(JobName.POPULATE_BOOK_BASIC_INFO)?.enabled ?: false
        if (isEnabled) {
            bookPopulateService.populateBasicBookInfoCollection()
        }
    }
}
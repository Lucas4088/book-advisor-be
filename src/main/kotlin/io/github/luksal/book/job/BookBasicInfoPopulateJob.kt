package io.github.luksal.book.job

import io.github.luksal.book.service.BookDataPopulationService
import io.github.luksal.book.job.dto.JobName
import io.github.luksal.ingestion.service.JobRunPolicyService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class BookBasicInfoPopulateJob(
    private val bookPopulateService: BookDataPopulationService,
    private val jobRunPolicyService: JobRunPolicyService
) {

    @Scheduled(fixedDelay = 1_000, scheduler = "bookBasicInfoPopulateJobScheduler")
    fun run() =
        jobRunPolicyService.isEnabled(JobName.POPULATE_BOOK_BASIC_INFO).takeIf { it }?.let {
             bookPopulateService.populateBasicBookInfoCollection()
        }

}
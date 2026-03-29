package io.github.luksal.ingestion.crawler.job

import io.github.luksal.book.job.dto.JobName
import io.github.luksal.commons.dto.EventStatus
import io.github.luksal.ingestion.service.BookRatingIngestionService
import io.github.luksal.ingestion.service.JobRunPolicyService
import io.github.luksal.util.ext.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class PageCrawlerScheduledJob(
    private val bookRatingIngestionService: BookRatingIngestionService,
    private val jobRunPolicyService: JobRunPolicyService,
) {
    companion object {
        val log = logger()
    }

    @Scheduled(fixedDelay = 60_000, scheduler = "pageCrawlerScheduledJobScheduler")
    fun processPending() =
        process(EventStatus.PENDING, JobName.CRAWL_BOOKS) {
            bookRatingIngestionService.processScheduledCrawlerTasks(it)
        }

    @Scheduled(fixedDelay = 120_000, scheduler = "pageCrawlerFailedJobScheduler")
    fun processFailed() =
        process(EventStatus.ERROR, JobName.CRAWL_BOOKS) {
            bookRatingIngestionService.processScheduledCrawlerTasks(it)
        }

    @Scheduled(fixedDelay = 500, scheduler = "pageCrawlerScheduledOnDemandJobScheduler")
    fun processOnDemandPending() =
        process(EventStatus.PENDING, JobName.CRAWL_BOOKS_ON_DEMAND) {
            bookRatingIngestionService.processScheduledCrawlerOnDemandTasks(it)
        }

    @Scheduled(fixedDelay = 4_000, scheduler = "pageCrawlerFailedOnDemandJobScheduler")
    fun processOnDemandFailed() =
        process(EventStatus.ERROR, JobName.CRAWL_BOOKS_ON_DEMAND) {
            bookRatingIngestionService.processScheduledCrawlerOnDemandTasks(it)
        }

    private fun process(eventStatus: EventStatus, jobName: JobName, action: (status: EventStatus) -> Unit) =
        jobRunPolicyService.isEnabled(jobName).takeIf { it }?.let {
            action.invoke(eventStatus)
        }
}
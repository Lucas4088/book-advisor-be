package io.github.luksal.ingestion.crawler.job

import io.github.luksal.book.job.dto.JobName
import io.github.luksal.commons.dto.EventStatus
import io.github.luksal.ingestion.crawler.service.PageCrawlerCrudService
import io.github.luksal.ingestion.service.BookRatingIngestionService
import io.github.luksal.ingestion.service.JobRunPolicyService
import io.github.luksal.util.ext.logger
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Component
import java.time.Instant
import kotlin.random.Random


@Component
class PageCrawlerScheduledJob(
    private val bookRatingIngestionService: BookRatingIngestionService,
    private val pageCrawlerCrudService: PageCrawlerCrudService,
    private val crawlersTaskSchedulerMap: Map<Long, TaskScheduler>,
    private val jobRunPolicyService: JobRunPolicyService
) {
    companion object {
        val log = logger()
    }

    @Scheduled(fixedDelay = 60_000, scheduler = "pageCrawlerScheduledJobScheduler")
    fun processPending() =
        process(EventStatus.PENDING, JobName.CRAWL_BOOKS, bookRatingIngestionService::crawlForRating)

    @Scheduled(fixedDelay = 120_000, scheduler = "pageCrawlerFailedJobScheduler")
    fun processFailed() =
        process(EventStatus.ERROR, JobName.CRAWL_BOOKS, bookRatingIngestionService::crawlForRating)

    @Scheduled(fixedDelay = 500, scheduler = "pageCrawlerScheduledOnDemandJobScheduler")
    fun processOnDemandPending() =
        process(EventStatus.PENDING, JobName.CRAWL_BOOKS_ON_DEMAND, bookRatingIngestionService::crawlForRatingOnDemand)

    @Scheduled(fixedDelay = 4_000, scheduler = "pageCrawlerFailedOnDemandJobScheduler")
    fun processOnDemandFailed() =
        process(EventStatus.ERROR, JobName.CRAWL_BOOKS_ON_DEMAND, bookRatingIngestionService::crawlForRatingOnDemand)


    private fun process(eventStatus: EventStatus, jobName: JobName, action: (EventStatus, Long) -> Unit) =
        jobRunPolicyService.isEnabled(jobName).takeIf { it }?.let {
            pageCrawlerCrudService.findAll().filter { it.enabled }.forEach {
                crawlersTaskSchedulerMap[it.id]?.apply {
                    val delaySeconds = Random.nextLong(1, 120) // 1–120
                    val nextExecutionTime = Instant.ofEpochMilli(System.currentTimeMillis() + delaySeconds * 1000)
                    schedule(
                        { action.invoke(eventStatus, it.id!!) },
                        nextExecutionTime
                    )
                }.also { ts ->
                    val scheduler = ts as ThreadPoolTaskScheduler
                    val executor = scheduler.scheduledExecutor as java.util.concurrent.ScheduledThreadPoolExecutor
                    log.info(
                        "Thread Scheduler info: ${scheduler.threadNamePrefix} : queueSize ${executor.queue.size} " +
                                ": completed ${executor.completedTaskCount}: active ${scheduler.activeCount}"
                    )
                }
            }
        }
}
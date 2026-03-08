package io.github.luksal.ingestion.crawler.job

import io.github.luksal.book.job.dto.JobName
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
    fun run() =
        jobRunPolicyService.isEnabled(JobName.CRAWL_BOOKS).takeIf { it }?.let {
            pageCrawlerCrudService.findAll().filter { it.enabled }.forEach {
                crawlersTaskSchedulerMap[it.id]?.apply {
                    val delaySeconds = Random.nextLong(1, 120) // 1–120
                    val nextExecutionTime = Instant.ofEpochMilli(System.currentTimeMillis() + delaySeconds * 1000)
                    schedule(
                        { bookRatingIngestionService.crawlForRating(it.id!!) },
                        nextExecutionTime
                    )
                }.also { ts ->
                    val scheduler = ts as ThreadPoolTaskScheduler
                    val executor = scheduler.scheduledExecutor as java.util.concurrent.ScheduledThreadPoolExecutor
                    log.info("Thread Scheduler info: ${scheduler.threadNamePrefix} : queueSize ${executor.queue.size} " +
                            ": completed ${executor.completedTaskCount}: active ${scheduler.activeCount}")
                }
            }
        }
}
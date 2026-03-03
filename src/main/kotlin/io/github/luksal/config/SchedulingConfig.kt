package io.github.luksal.config

import io.github.luksal.ingestion.crawler.jpa.PageCrawlerJpaRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.util.concurrent.ConcurrentHashMap

@Configuration
@EnableScheduling
class SchedulingConfig(
    private val crawlerJpaRepository: PageCrawlerJpaRepository
) {

    @Bean
    @EventListener(ApplicationReadyEvent::class)
    fun crawlersTaskSchedulerMap(): Map<Long, TaskScheduler> =
        ConcurrentHashMap(
            crawlerJpaRepository.findAll()
                .associateBy({ it.id },
                    { crawler -> scheduler(crawler.name) })
        )

    @Bean(name = ["bookBasicInfoPopulateJobScheduler"])
    fun bookBasicInfoPopulateJobScheduler(): TaskScheduler =
        ThreadPoolTaskScheduler().apply {
            poolSize = 1
            setThreadNamePrefix("bbipjScheduler-")
            initialize()
        }

    @Bean(name = ["bookDetailsPopulateJobScheduler"])
    fun bookDetailsPopulateJobScheduler(): TaskScheduler =
        ThreadPoolTaskScheduler().apply {
            poolSize = 1
            setThreadNamePrefix("bdpjScheduler-")
            initialize()
        }

    @Bean(name = ["pageCrawlerScheduledJobScheduler"])
    fun pageCrawlerScheduledJobScheduler(): TaskScheduler =
        ThreadPoolTaskScheduler().apply {
            poolSize = 1
            setThreadNamePrefix("pcsjScheduler-")
            initialize()
        }

    private fun scheduler(name: String): TaskScheduler =
        ThreadPoolTaskScheduler().apply {
            poolSize = 1
            setThreadNamePrefix("$name-scheduler-")
            initialize()
        }
}
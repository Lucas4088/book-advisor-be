package io.github.luksal.statistics.job

import io.github.luksal.event.service.EventService
import io.github.luksal.statistics.service.StatisticsService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class BookDetailsFetchedStatisticsSSeJob(
    private val service: StatisticsService,
    private val eventService: EventService
) {


    @Scheduled(cron = "*/10 * * * * *")
    fun run() =
        service.calculateBookDetailsFetchedStatistics()
            .let {
                eventService.emit("book-details-fetched-statistics", it)
            }
}
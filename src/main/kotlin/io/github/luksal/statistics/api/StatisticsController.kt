package io.github.luksal.statistics.api

import io.github.luksal.statistics.service.StatisticsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/statistics")
class StatisticsController(private val statisticsService: StatisticsService) {

    @GetMapping("/book")
    fun calculateBookStatistics() = statisticsService.calculateBookStatistics()

    @GetMapping("/crawler-event")
    fun calculateCrawlerEventStatusStatistics() = statisticsService.calculateCrawlerEventStatistics()

    @GetMapping("/book-rating")
    fun calculateBooksRatingStats() = statisticsService.calculateBooksRatingStatistics()

    @GetMapping("/book-details-fetched")
    fun calculateBookDetailsFetched() = statisticsService.calculateBookDetailsFetchedStatistics()
}
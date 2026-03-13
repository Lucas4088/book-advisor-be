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

    @GetMapping("/crawler-event-status")
    fun calculateCrawlerEventStatusStatistics() = statisticsService.calculateCrawlerEventStatistics()
}

/*
 Total amount of book details
 Total amount of Authors
 Total amount of book documents
 Total amount of Books
 Percentage of Ratings/crawler with total ratings count

 Percentage of Book Documents / Book Details

 Percentage of Books/Book documents
 states of crawler events pie chart


 const entries: PieChartEntry[] = [
     {name: "Sci-Fi", value: 120, fill: "#6366F1"},
     {name: "Fantasy", value: 90, fill: "#10B981"},
     {name: "Drama", value: 40, fill: "#F59E0B"}
 ]
*/
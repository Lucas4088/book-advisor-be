package io.github.luksal.book.api

import io.github.luksal.book.service.RatingService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/rating"])
class RatingController(
    private val ratingService: RatingService
) {

    @GetMapping("/book/{bookId}/schedule-on-demand")
    fun scheduleOnDemandRatingRetrieval(@PathVariable bookId: String) {
        ratingService.scheduleOnDemandRatingRetrieval(bookId)
    }
}
package io.github.luksal.ingestion.api

import io.github.luksal.book.service.BookDataPopulationService
import io.github.luksal.ingestion.api.dto.JobRunPolicy
import io.github.luksal.ingestion.api.dto.ScheduleBookBasicInfoRequest
import io.github.luksal.ingestion.api.dto.ScheduledBookBasicInfoPopulationEvent
import io.github.luksal.ingestion.api.dto.ScheduledBookBasicInfoSearchRequest
import io.github.luksal.book.job.dto.JobName
import io.github.luksal.ingestion.service.JobRunPolicyService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping(value = ["/api/population-management"])
class PopulationManagementController(
    private val bookDataPopulationService: BookDataPopulationService,
    private val jobRunPolicyService: JobRunPolicyService,
    private val customInitializerDispatcher: CoroutineDispatcher,
) {

    @PutMapping("/job-run-policy")
    fun setPolicy(@RequestBody request: JobRunPolicy) =
        jobRunPolicyService.setPolicy(request)


    @GetMapping("/job-run-policy/{name}")
    fun findPolicy(@PathVariable name: JobName): JobRunPolicy? =
        jobRunPolicyService.findPolicy(name)

    @PostMapping(path = ["/schedule/book-basic-info"])
    fun scheduleBookBasicDataPopulation(@RequestBody request: ScheduleBookBasicInfoRequest) {
        return bookDataPopulationService.scheduleBasicBookInfoCollection(
            fromYear = request.fromYear,
            toYear = request.toYear,
            lang = request.lang
        )
    }

    @PostMapping("/book-basic-info-schedule")
    fun fetchScheduledBookBasicInfo(
        @RequestBody request: ScheduledBookBasicInfoSearchRequest,
        page: Pageable,
    ): Page<ScheduledBookBasicInfoPopulationEvent> {
        return bookDataPopulationService.searchBasicBookInfoSchedule(request, page)
    }

    @PostMapping("/book-basic-info/populate")
    fun populateBasicBookInfoCollection() {
        CoroutineScope(customInitializerDispatcher).launch {
            bookDataPopulationService.populateBasicBookInfoCollection()
        }
    }

    @PostMapping("/book-details/populate")
    fun populateBooksCollection() {
        CoroutineScope(customInitializerDispatcher).launch {
            bookDataPopulationService.populateBooksCollection()
        }
    }
}
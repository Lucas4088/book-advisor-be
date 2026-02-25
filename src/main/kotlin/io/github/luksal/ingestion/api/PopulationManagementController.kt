package io.github.luksal.ingestion.api

import io.github.luksal.book.service.BookDataPopulationService
import io.github.luksal.ingestion.api.dto.JobRunPolicy
import io.github.luksal.ingestion.api.dto.ScheduledBasicInfoSearchRequest
import io.github.luksal.ingestion.api.dto.ScheduledBookBasicInfoPopulationEvent
import io.github.luksal.ingestion.job.dto.JobName
import io.github.luksal.ingestion.jpa.JobRunPolicyEntity
import io.github.luksal.ingestion.jpa.JobRunPolicyRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping(value = ["/api/population-management"])
class PopulationManagementController(
    private val bookDataPopulationService: BookDataPopulationService,
    private val jobRunPolicyRepository: JobRunPolicyRepository,
    private val customInitializerDispatcher: CoroutineDispatcher,
) {

    @PutMapping("/job-run-policy")
    fun setPolicy(@RequestBody request: JobRunPolicy) {
        val entity = jobRunPolicyRepository.findByName(request.name)
            ?.apply {
                enabled = request.enabled
            }
            ?: JobRunPolicyEntity(
                name = request.name,
                enabled = request.enabled,
                updatedAt = System.currentTimeMillis()
            )

        jobRunPolicyRepository.save(entity)
    }

    @GetMapping("/job-run-policy/{name}")
    fun findPolicy(@PathVariable name: JobName): JobRunPolicy? =
        jobRunPolicyRepository.findByName(name)?.let {
            JobRunPolicy(name = it.name, enabled = it.enabled)
        }

    @PostMapping(path = ["/book-basic-info-schedule"], params = ["fromYear", "toYear", "lang"])
    fun scheduleBookBasicDataPopulation(fromYear: Int, toYear: Int, lang: String) {
        return bookDataPopulationService.scheduleBasicBookInfoCollection(
            fromYear = fromYear,
            toYear = toYear,
            lang = lang
        )
    }

    @GetMapping("/book-basic-info-schedule")
    fun fetchScheduleBookBasicInfo(
        @RequestBody request: ScheduledBasicInfoSearchRequest,
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
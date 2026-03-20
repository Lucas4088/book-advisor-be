package io.github.luksal.integration.job

import io.github.luksal.event.service.EventService
import io.github.luksal.integration.dto.IntegrationSourceStatus
import io.github.luksal.integration.dto.IntegrationSourceStatusDto
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class IntegrationSourceAvailabilityJob(
    private val eventService: EventService,
    private val circuitBreakerRegistry: CircuitBreakerRegistry
) {

    companion object {
        private val CB_INTEGRATION_SOURCE_NAMES = mapOf(
            "search-googleBooksCircuitBreaker" to "Google Books",
            "search-archiveBooksCircuitBreaker" to "Archive Books",
            "search-openLibraryCircuitBreaker" to  "Open Library"
        )
    }

    @Scheduled(cron = "* * * * * *")
    fun run() =
        CB_INTEGRATION_SOURCE_NAMES.map {
            circuitBreakerRegistry.circuitBreaker(it.key)
        }.map { IntegrationSourceStatusDto(
            CB_INTEGRATION_SOURCE_NAMES[it.name]!!,
            resolveStatus(it.state)
        ) }.let {
            eventService.emit("integration-source-status", it)
        }

    private fun resolveStatus(cbStatus: CircuitBreaker.State): IntegrationSourceStatus =
        when (cbStatus) {
            CircuitBreaker.State.OPEN -> IntegrationSourceStatus.UNAVAILABLE
            CircuitBreaker.State.CLOSED -> IntegrationSourceStatus.AVAILABLE
            CircuitBreaker.State.HALF_OPEN -> IntegrationSourceStatus.SEMI_AVAILABLE
            else -> IntegrationSourceStatus.UNKNOWN
        }
}
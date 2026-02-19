package io.github.luksal.ingestion.api

import io.github.luksal.ingestion.jpa.JobRunPolicyEntity
import io.github.luksal.ingestion.jpa.JobRunPolicyRepository
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping(value = ["/api/job-run-policy"])
class PopulationManagementController(val jobRunPolicyRepository: JobRunPolicyRepository) {

    @PutMapping
    fun setPolicy(@RequestBody request: JobRunPolicyRequest) {
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
}
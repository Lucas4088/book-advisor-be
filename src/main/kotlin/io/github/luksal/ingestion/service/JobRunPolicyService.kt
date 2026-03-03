package io.github.luksal.ingestion.service

import io.github.luksal.ingestion.api.dto.JobRunPolicy
import io.github.luksal.book.job.dto.JobName
import io.github.luksal.ingestion.jpa.JobRunPolicyEntity
import io.github.luksal.ingestion.jpa.JobRunPolicyRepository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody
import java.time.Instant

@Service
class JobRunPolicyService(private val jobRunPolicyRepository: JobRunPolicyRepository) {

    fun findPolicy(jobName: JobName): JobRunPolicy? =
        jobRunPolicyRepository.findByName(jobName)?.let {
            JobRunPolicy(
                name = it.name,
                enabled = it.enabled
            )
        }

    //TODO fix
    //@Cacheable(value = "jobRunPolicy")
    fun isEnabled(jobName: JobName): Boolean =
        jobRunPolicyRepository.findByName(jobName)?.enabled ?: false

    //@CacheEvict(value="jobRunPolicy", key="#request.name")
    fun setPolicy(@RequestBody request: JobRunPolicy) {
        val entity = jobRunPolicyRepository.findByName(request.name)
            ?.apply {
                enabled = request.enabled
            }
            ?: JobRunPolicyEntity(
                name = request.name,
                enabled = request.enabled,
                updatedAt = Instant.now()
            )

        jobRunPolicyRepository.save(entity)
    }
}
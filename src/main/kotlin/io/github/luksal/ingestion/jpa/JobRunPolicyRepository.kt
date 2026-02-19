package io.github.luksal.ingestion.jpa

import io.github.luksal.ingestion.job.dto.JobName
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JobRunPolicyRepository : JpaRepository<JobRunPolicyEntity, Long> {
    fun findByName(name: JobName): JobRunPolicyEntity?
}
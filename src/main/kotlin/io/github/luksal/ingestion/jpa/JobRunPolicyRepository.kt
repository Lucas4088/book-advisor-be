package io.github.luksal.ingestion.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JobRunPolicyRepository : JpaRepository<JobRunPolicy, Long> {

}
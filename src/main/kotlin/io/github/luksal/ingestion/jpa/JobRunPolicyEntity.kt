package io.github.luksal.ingestion.jpa

import io.github.luksal.book.job.dto.JobName
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "job_run_policy")
data class JobRunPolicyEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    val name: JobName,

    @Column
    var enabled: Boolean,

    @Column
    val updatedAt: Instant

)
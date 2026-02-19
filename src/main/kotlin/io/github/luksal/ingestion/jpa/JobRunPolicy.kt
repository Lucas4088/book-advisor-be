package io.github.luksal.ingestion.jpa

import io.github.luksal.ingestion.job.dto.JobName
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class JobRunPolicy(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    @Column(unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    val name: JobName,

    @Column
    var enabled: Boolean,

    @Column
    val updatedAt: Long

)
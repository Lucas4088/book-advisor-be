package io.github.luksal.ingestion.api.dto

import io.github.luksal.book.job.dto.JobName

data class JobRunPolicy(val name: JobName, val enabled: Boolean)
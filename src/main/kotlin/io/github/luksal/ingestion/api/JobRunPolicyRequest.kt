package io.github.luksal.ingestion.api

import io.github.luksal.ingestion.job.dto.JobName

data class JobRunPolicyRequest(val name: JobName, val enabled: Boolean)
package io.github.luksal.integration.dto

data class IntegrationSourceStatusDto(
    val name: String,
    val status: IntegrationSourceStatus
)

enum class IntegrationSourceStatus {
    AVAILABLE,
    UNAVAILABLE,
    SEMI_AVAILABLE,
    UNKNOWN,
}

package io.github.luksal.commons.jpa

import jakarta.persistence.Column
import java.time.Instant

class RetryMeta(
    @Column(name = "retry_count", nullable = false)
    var count: Int = 0,
    @Column(name = "last_retry_at")
    var lastRetryAt: Instant? = null,
    @Column(name = "next_retry_at")
    var nextRetryAt: Instant? = null
) {

    fun increment() = count++
}
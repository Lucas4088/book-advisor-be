package io.github.luksal.commons.dto

import java.time.Instant

interface RetryableEvent {
    fun getRetryCount(): Int
    fun incrementRetryCount() : Int
    fun resetStatus()
    fun setNextRetryAt(nextRetryAt: Instant)
}
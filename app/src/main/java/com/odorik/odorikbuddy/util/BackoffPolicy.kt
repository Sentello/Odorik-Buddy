package com.odorik.odorikbuddy.util

/**
 * Delay schedule for bounded automatic retries: exponential growth from
 * [initialDelayMs], capped at [maxDelayMs], at most [maxAttempts] attempts.
 */
data class BackoffPolicy(
    val initialDelayMs: Long = 5_000,
    val maxDelayMs: Long = 60_000,
    val maxAttempts: Int = 5
) {
    /** Delay before the given 1-based attempt. */
    fun delayBeforeAttempt(attempt: Int): Long {
        require(attempt >= 1) { "attempt is 1-based" }
        val shift = (attempt - 1).coerceAtMost(20) // avoid Long overflow
        return (initialDelayMs shl shift).coerceAtMost(maxDelayMs)
    }
}

package com.odorik.odorikbuddy.util


data class BackoffPolicy(
    val initialDelayMs: Long = 5_000,
    val maxDelayMs: Long = 60_000,
    val maxAttempts: Int = 5
) {

    fun delayBeforeAttempt(attempt: Int): Long {
        require(attempt >= 1) { "attempt is 1-based" }
        val shift = (attempt - 1).coerceAtMost(20)
        return (initialDelayMs shl shift).coerceAtMost(maxDelayMs)
    }
}

package com.odorik.odorikbuddy.util

import org.junit.Assert.assertEquals
import org.junit.Test

class BackoffPolicyTest {

    private val policy = BackoffPolicy(initialDelayMs = 5_000, maxDelayMs = 60_000, maxAttempts = 5)

    @Test
    fun `delays grow exponentially and cap at max`() {
        assertEquals(5_000L, policy.delayBeforeAttempt(1))
        assertEquals(10_000L, policy.delayBeforeAttempt(2))
        assertEquals(20_000L, policy.delayBeforeAttempt(3))
        assertEquals(40_000L, policy.delayBeforeAttempt(4))
        assertEquals(60_000L, policy.delayBeforeAttempt(5))
        assertEquals(60_000L, policy.delayBeforeAttempt(12))
    }

    @Test
    fun `huge attempt numbers stay capped (no overflow)`() {
        assertEquals(60_000L, policy.delayBeforeAttempt(1_000))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `attempt is one-based`() {
        policy.delayBeforeAttempt(0)
    }
}

package com.odorik.odorikbuddy.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class ApiDatesTest {

    @Test
    fun `formats instant as UTC with Z suffix`() {
        val instant = Instant.parse("2026-07-17T08:30:00Z")
        assertEquals("2026-07-17T08:30:00Z", ApiDates.formatUtc(instant))
    }

    @Test
    fun `parses offset timestamp to correct instant`() {
        val parsed = ApiDates.parse("2026-07-17T10:30:00+02:00")
        assertEquals(Instant.parse("2026-07-17T08:30:00Z"), parsed)
    }

    @Test
    fun `parses Z timestamp`() {
        val parsed = ApiDates.parse("2026-07-17T08:30:00Z")
        assertEquals(Instant.parse("2026-07-17T08:30:00Z"), parsed)
    }

    @Test
    fun `format then parse round-trips`() {
        val instant = Instant.parse("2026-01-05T23:59:59Z")
        assertEquals(instant, ApiDates.parse(ApiDates.formatUtc(instant)))
    }

    @Test
    fun `malformed input returns null`() {
        assertNull(ApiDates.parse("not-a-date"))
        assertNull(ApiDates.parse("2026-07-17"))
    }
}

package com.odorik.odorikbuddy.util

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 * Locale-invariant date handling for the Odorik API.
 * Timestamps are SENT as UTC ISO-8601 ("...Z") and PARSED with any offset
 * ("+02:00", "Z", ...), matching what the API returns.
 */
object ApiDates {

    private val UTC_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).withZone(ZoneOffset.UTC)

    /** Formats an instant as UTC ISO-8601 for API query parameters. */
    fun formatUtc(instant: Instant): String = UTC_FORMATTER.format(instant)

    /** Parses an API ISO-8601 timestamp with any offset; null when malformed. */
    fun parse(isoDate: String): Instant? = try {
        OffsetDateTime.parse(isoDate).toInstant()
    } catch (e: DateTimeParseException) {
        null
    }
}

package com.odorik.odorikbuddy.util

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale


object ApiDates {

    private val UTC_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).withZone(ZoneOffset.UTC)


    fun formatUtc(instant: Instant): String = UTC_FORMATTER.format(instant)


    fun parse(isoDate: String): Instant? = try {
        OffsetDateTime.parse(isoDate).toInstant()
    } catch (e: DateTimeParseException) {
        null
    }
}

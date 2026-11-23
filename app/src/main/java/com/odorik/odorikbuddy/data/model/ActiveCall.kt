package com.odorik.odorikbuddy.data.model

import com.google.gson.annotations.SerializedName

/**
 * A call in progress (`/active_calls.json`). Odorik reports no direction and no duration:
 * duration is derived from [answerDate]; `null` [answerDate] means it's still ringing.
 *
 * Everything except [id] is nullable on purpose — Gson ignores Kotlin nullability and a
 * missing field must not crash the UI. Every field has a default so Kotlin generates the
 * no-arg constructor that proguard-rules.pro keeps — without it R8 treats the class as
 * never instantiated and the release build fails on the first call in the list.
 */
data class ActiveCall(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("source_number") val sourceNumber: String? = null,
    @SerializedName("destination_number") val destinationNumber: String? = null,
    @SerializedName("destination_name") val destinationName: String? = null,
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("answer_date") val answerDate: String? = null,
    @SerializedName("price_per_minute") val pricePerMinute: Double? = null,
    @SerializedName("line") val line: Long? = null
) {
    val isAnswered: Boolean get() = !answerDate.isNullOrBlank()

    /** Legs of one call share caller and start time; calls without them never merge. */
    val legKey: String
        get() = if (sourceNumber.isNullOrBlank() || startDate.isNullOrBlank()) "id:$id"
        else "$sourceNumber|$startDate"

    /** Internal leg to a line, e.g. `*760490` (the forwarding to the user's IP phone). */
    val isInternalLeg: Boolean get() = destinationNumber?.startsWith("*") == true
}

/**
 * One incoming call shows up as several legs (real call 2026-11-22: caller → public number
 * "Česko v síti- presmerovano" + caller → `*760490` "Česká rep. - * v síti", same start
 * second). Keeps one entry per call: the leg to the public number, else the first leg.
 * Hanging up that leg ended the whole call (verified the same day).
 */
fun List<ActiveCall>.collapseLegs(): List<ActiveCall> =
    groupBy { it.legKey }.values.map { legs -> legs.firstOrNull { !it.isInternalLeg } ?: legs.first() }

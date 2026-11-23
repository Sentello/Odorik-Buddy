package com.odorik.odorikbuddy.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// Represents a single item in the call or SMS history.
// Modernized with camelCase properties + explicit @SerializedName for Gson
// and @ColumnInfo to keep Room schema stable.
@Entity(tableName = "history")
data class HistoryItem(
    @PrimaryKey
    @SerializedName("id")
    @ColumnInfo(name = "id")
    val id: String = "",

    @SerializedName("date")
    @ColumnInfo(name = "date")
    val date: String = "",

    @SerializedName("direction")
    @ColumnInfo(name = "direction")
    val direction: String = "",

    @SerializedName("source_number")
    @ColumnInfo(name = "source_number")
    val sourceNumber: String = "",

    @SerializedName("destination_number")
    @ColumnInfo(name = "destination_number")
    val destinationNumber: String = "",

    @SerializedName("length")
    @ColumnInfo(name = "length")
    val length: Int? = null, // Nullable because SMS records do not have a length

    @SerializedName("ringing_length")
    @ColumnInfo(name = "ringing_length")
    val ringingLength: Int? = null,

    @SerializedName("price")
    @ColumnInfo(name = "price")
    val price: Double = 0.0,

    @SerializedName("price_per_minute")
    @ColumnInfo(name = "price_per_minute")
    val pricePerMinute: Double? = null,

    @SerializedName("status")
    @ColumnInfo(name = "status")
    val status: String? = null,

    @SerializedName("destination_name")
    @ColumnInfo(name = "destination_name")
    val destinationName: String? = null,

    @SerializedName("redirection_parent_id")
    @ColumnInfo(name = "redirection_parent_id")
    val redirectionParentId: String? = null,

    @SerializedName("line")
    @ColumnInfo(name = "line")
    val line: Int? = null,

    @SerializedName("recording")
    @ColumnInfo(name = "recording")
    val recording: String? = null,

    @SerializedName("event_type")
    @ColumnInfo(name = "event_type")
    val eventTypeRaw: String? = null
) {
    val eventType: String
        get() = eventTypeRaw ?: if (length != null) "call" else "sms"

    val isCall: Boolean
        get() = eventType == "call"

    val isSms: Boolean
        get() = eventType == "sms"

    val isIncoming: Boolean
        get() = direction == "in"

    val isOutgoing: Boolean
        get() = direction == "out" || direction == "redirected"

    /**
     * The leg that just rings the account's own line (`*760490`) for an incoming call. Not a
     * real forwarding: same length as the parent, free, and it made every incoming call show
     * up twice (seen 2026-11-22: parent "in" → public number, child "redirected" → `*760490`).
     */
    val isInternalLineLeg: Boolean
        get() = redirectionParentId != null && destinationNumber.startsWith("*")
}

/** A call between the account's own lines: both numbers are internal (`*760490`). */
private val HistoryItem.isLineToLine: Boolean
    get() = isCall && sourceNumber.startsWith("*") && destinationNumber.startsWith("*")

/**
 * Removes the duplicates Odorik writes for one call, so each call shows once:
 * - [HistoryItem.isInternalLineLeg]: the leg that rings the own line under an incoming call;
 * - the "in" mirror of a call between own lines: Odorik logs `*A → *B` as "out" and again as
 *   "in" (same numbers, ~1 s apart, seen 2026-11-22). The "out" record (which carries the
 *   price) stays.
 * Real forwardings to other numbers are kept.
 */
fun List<HistoryItem>.withoutInternalLineLegs(): List<HistoryItem> {
    val outgoingLineCalls = filter { it.isLineToLine && it.direction == "out" }
    fun isMirror(item: HistoryItem): Boolean {
        if (!item.isLineToLine || item.direction != "in") return false
        val at = com.odorik.odorikbuddy.util.ApiDates.parse(item.date) ?: return false
        return outgoingLineCalls.any { out ->
            out.sourceNumber == item.sourceNumber &&
                out.destinationNumber == item.destinationNumber &&
                com.odorik.odorikbuddy.util.ApiDates.parse(out.date)
                    ?.let { kotlin.math.abs(it.epochSecond - at.epochSecond) <= 3 } == true
        }
    }
    return filterNot { it.isInternalLineLeg || isMirror(it) }
}

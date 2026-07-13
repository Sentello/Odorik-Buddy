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
    val recording: String? = null
) {
    val eventType: String
        get() = if (length != null) "call" else "sms"

    val isCall: Boolean
        get() = length != null

    val isSms: Boolean
        get() = length == null

    val isIncoming: Boolean
        get() = direction == "in"

    val isOutgoing: Boolean
        get() = direction == "out" || direction == "redirected"
}

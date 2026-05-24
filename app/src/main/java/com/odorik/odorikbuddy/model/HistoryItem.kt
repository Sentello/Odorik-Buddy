package com.odorik.odorikbuddy.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName



@Entity(tableName = "history")
data class HistoryItem(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("direction")
    val direction: String,
    @SerializedName("source_number")
    val source_number: String,
    @SerializedName("destination_number")
    val destination_number: String,
    @SerializedName("length")
    val length: Int?,
    @SerializedName("ringing_length")
    val ringing_length: Int?,
    @SerializedName("price")
    val price: Double,
    @SerializedName("price_per_minute")
    val price_per_minute: Double?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("destination_name")
    val destination_name: String?,
    @SerializedName("redirection_parent_id")
    val redirection_parent_id: String?,
    @SerializedName("line")
    val line: Int?,
    @SerializedName("recording")
    val recording: String?
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

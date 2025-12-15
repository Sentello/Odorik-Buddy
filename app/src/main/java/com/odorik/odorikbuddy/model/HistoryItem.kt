package com.odorik.odorikbuddy.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// Represents a single item in the call or SMS history.
// Using @SerializedName for robust JSON parsing with Gson.
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
    val length: Int?, // Nullable because SMS records do not have a length
    @SerializedName("ringing_length")
    val ringing_length: Int?,
    @SerializedName("price")
    val price: Double,
    @SerializedName("status")
    val status: String?, // Nullable because SMS records do not have a status
    @SerializedName("destination_name")
    val destination_name: String?,
    @SerializedName("redirection_parent_id")
    val redirection_parent_id: String?
)

package com.odorik.odorikbuddy.model

import com.google.gson.annotations.SerializedName



data class HistoryItem(
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
    @SerializedName("price")
    val price: Double,
    @SerializedName("status")
    val status: String? 
)

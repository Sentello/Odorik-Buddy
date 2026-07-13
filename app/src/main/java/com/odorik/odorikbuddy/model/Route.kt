package com.odorik.odorikbuddy.model

import com.google.gson.annotations.SerializedName

data class Route(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("public_number") val publicNumber: String = "",
    @SerializedName("source_number") val sourceNumber: String = "",
    @SerializedName("ringing_number") val ringingNumber: String = ""
)
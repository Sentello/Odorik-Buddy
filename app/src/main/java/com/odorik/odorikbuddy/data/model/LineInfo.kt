package com.odorik.odorikbuddy.data.model

import com.google.gson.annotations.SerializedName

data class LineInfo(
    @SerializedName("line_name") val lineName: String,
    @SerializedName("caller_id") val callerId: String,
    val password: String
)
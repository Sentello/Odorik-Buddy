package com.odorik.odorikbuddy.data.model

import com.google.gson.annotations.SerializedName

data class CallInfo(
    @SerializedName("call_id") val callId: String,
    @SerializedName("caller_id") val callerId: String,
    @SerializedName("called_number") val calledNumber: String,
    @SerializedName("start_time") val startTime: String,
    val duration: String,
    val cost: String
)
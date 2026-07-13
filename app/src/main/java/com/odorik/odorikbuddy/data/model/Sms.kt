package com.odorik.odorikbuddy.data.model

import com.google.gson.annotations.SerializedName

data class Sms(
    @SerializedName("recipient") val recipient: String = "",
    @SerializedName("message") val message: String = ""
)
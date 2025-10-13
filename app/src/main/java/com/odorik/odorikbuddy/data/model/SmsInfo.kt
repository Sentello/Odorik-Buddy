package com.odorik.odorikbuddy.data.model

import com.google.gson.annotations.SerializedName

data class SmsInfo(
    @SerializedName("sms_id") val smsId: String,
    val recipient: String,
    val message: String,
    @SerializedName("sent_time") val sentTime: String,
    val status: String
)
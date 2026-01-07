package com.odorik.odorikbuddy.model

import com.google.gson.annotations.SerializedName

data class AppUpdateInfo(
    @SerializedName("version")
    val version: String,
    @SerializedName("download_url")
    val downloadUrl: String,
    @SerializedName("message")
    val message: String
)
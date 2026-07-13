package com.odorik.odorikbuddy.data.model

import com.google.gson.annotations.SerializedName

data class ConnectedDevice(
    @SerializedName("user_agent") val userAgent: String = "",
    @SerializedName("public_socket") val publicSocket: String = ""
)

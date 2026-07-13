package com.odorik.odorikbuddy.data.model

import com.google.gson.annotations.SerializedName

data class Line(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("caller_id") val callerId: String = "",
    @SerializedName("public_number") val publicNumber: String? = null,
    @SerializedName("sip_password") val sipPassword: String = "",
    @SerializedName("connected_devices") val connectedDevices: List<ConnectedDevice> = emptyList()
)

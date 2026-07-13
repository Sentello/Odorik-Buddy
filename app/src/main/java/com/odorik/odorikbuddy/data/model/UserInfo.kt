package com.odorik.odorikbuddy.data.model

import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName("name") val name: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("phone_number") val phoneNumber: String = ""
)
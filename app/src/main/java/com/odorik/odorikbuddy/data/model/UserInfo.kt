package com.odorik.odorikbuddy.data.model

import com.google.gson.annotations.SerializedName

data class UserInfo(
    val name: String,
    val email: String,
    @SerializedName("phone_number") val phoneNumber: String
)
package com.odorik.odorikbuddy.model

import com.google.gson.annotations.SerializedName

data class PublicNumber(
    @SerializedName("public_number")
    val publicNumber: String,
    val type: String
)

package com.odorik.odorikbuddy.data.model

import com.google.gson.annotations.SerializedName

data class Credit(
    @SerializedName("credit") val credit: String = "",
    @SerializedName("currency") val currency: String = ""
)
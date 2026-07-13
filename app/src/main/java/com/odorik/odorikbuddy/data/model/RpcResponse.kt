package com.odorik.odorikbuddy.data.model

import com.google.gson.annotations.SerializedName

data class RpcResponse(
    @SerializedName("result") val result: String? = null,
    @SerializedName("error") val error: String? = null
)
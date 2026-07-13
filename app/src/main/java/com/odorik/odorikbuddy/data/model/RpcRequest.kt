package com.odorik.odorikbuddy.data.model

import com.google.gson.annotations.SerializedName

data class RpcRequest(
    @SerializedName("method") val method: String = "",
    @SerializedName("params") val params: List<Any> = emptyList()
)
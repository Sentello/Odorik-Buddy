package com.odorik.odorikbuddy.data.model

data class Line(
    val id: Int,
    val name: String,
    val caller_id: String,
    val public_number: String?,
    val sip_password: String,
    val connected_devices: List<ConnectedDevice>
)

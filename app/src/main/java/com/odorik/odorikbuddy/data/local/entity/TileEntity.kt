package com.odorik.odorikbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tiles")
data class TileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val position: Int,
    val label: String,
    val recipient: String,
    val callType: String, // "CALLBACK" or "ONESHOT"
    val lineId: String?, // For callback
    val callerId: String?, // For callback
    val useLineAsCallerId: Boolean = false, // For oneshot
    val color: Long? = null, // Color as Long (ARGB) - Background
    val textColor: Long? = null, // Text Color as Long (ARGB)
    val widgetStyle: String = "SQUARE" // "SQUARE" or "CIRCLE"
)

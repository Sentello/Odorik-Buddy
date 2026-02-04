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
    val callType: String, 
    val lineId: String?, 
    val callerId: String?, 
    val useLineAsCallerId: Boolean = false, 
    val color: Long? = null, 
    val textColor: Long? = null, 
    val widgetStyle: String = "SQUARE" 
)

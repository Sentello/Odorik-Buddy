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
    val callType: TileCallType, // stored as "CALLBACK" or "ONESHOT"
    val lineId: String?, // For callback
    val callerId: String?, // For callback
    val useLineAsCallerId: Boolean = false, // For oneshot
    val color: Long? = null, // Color as Long (ARGB) - Background
    val textColor: Long? = null, // Text Color as Long (ARGB)
    // Unused. The widget keeps its own style per widget (see TileWidgetStyle). The field
    // stays only because the database column exists; removing it would need a migration
    // that rebuilds the tiles table.
    val widgetStyle: String = "SQUARE"
)

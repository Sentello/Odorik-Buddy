package com.odorik.odorikbuddy.ui.calls

import com.odorik.odorikbuddy.data.local.entity.TileCallType

/** What the add/edit tile dialog collects; everything the user can set on a tile. */
data class TileData(
    val label: String,
    val recipient: String,
    val callType: TileCallType,
    val lineId: String?,
    val callerId: String?,
    val useLineAsCallerId: Boolean,
    val color: Long?,
    val textColor: Long?
)

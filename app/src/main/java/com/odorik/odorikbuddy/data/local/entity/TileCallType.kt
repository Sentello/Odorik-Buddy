package com.odorik.odorikbuddy.data.local.entity

import androidx.room.TypeConverter

/** How a tile places its call. The enum name is the value stored in the database. */
enum class TileCallType {
    CALLBACK,
    ONESHOT;

    companion object {
        /**
         * Unknown or missing values fall back to ONESHOT - the same as before this was an
         * enum, when everything that wasn't "CALLBACK" was treated as a one-shot call.
         */
        fun fromKey(key: String?): TileCallType =
            values().firstOrNull { it.name == key } ?: ONESHOT
    }
}

/** Stores [TileCallType] as its name, so the `tiles.callType` TEXT column is unchanged. */
class TileCallTypeConverter {
    @TypeConverter
    fun toKey(type: TileCallType): String = type.name

    @TypeConverter
    fun fromKey(key: String): TileCallType = TileCallType.fromKey(key)
}

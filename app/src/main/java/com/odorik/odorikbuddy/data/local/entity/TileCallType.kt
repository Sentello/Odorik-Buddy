package com.odorik.odorikbuddy.data.local.entity

import androidx.room.TypeConverter

enum class TileCallType {
    CALLBACK,
    ONESHOT;

    companion object {
        fun fromKey(key: String?): TileCallType =
            values().firstOrNull { it.name == key } ?: ONESHOT
    }
}

class TileCallTypeConverter {
    @TypeConverter
    fun toKey(type: TileCallType): String = type.name

    @TypeConverter
    fun fromKey(key: String): TileCallType = TileCallType.fromKey(key)
}

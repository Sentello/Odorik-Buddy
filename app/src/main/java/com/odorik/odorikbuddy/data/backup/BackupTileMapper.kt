package com.odorik.odorikbuddy.data.backup

import com.odorik.odorikbuddy.data.local.entity.TileCallType
import com.odorik.odorikbuddy.data.local.entity.TileEntity

object BackupTileMapper {

    fun toBackup(tile: TileEntity) = BackupTile(
        id = tile.id,
        position = tile.position,
        label = tile.label,
        recipient = tile.recipient,
        callType = tile.callType.name,
        lineId = tile.lineId,
        callerId = tile.callerId,
        useLineAsCallerId = tile.useLineAsCallerId,
        color = tile.color,
        textColor = tile.textColor
    )

    fun toEntities(tiles: List<BackupTile>): List<TileEntity> {
        val usedIds = mutableSetOf<Int>()
        return tiles
            .withIndex()
            .sortedWith(compareBy({ it.value.position ?: Int.MAX_VALUE }, { it.index }))
            .mapIndexed { newPosition, (_, tile) ->
                val id = tile.id?.takeIf { it > 0 && usedIds.add(it) } ?: 0
                TileEntity(
                    id = id,
                    position = newPosition,
                    label = tile.label.orEmpty(),
                    recipient = tile.recipient.orEmpty(),
                    callType = TileCallType.fromKey(tile.callType),
                    lineId = tile.lineId,
                    callerId = tile.callerId,
                    useLineAsCallerId = tile.useLineAsCallerId ?: false,
                    color = tile.color,
                    textColor = tile.textColor
                )
            }
    }
}

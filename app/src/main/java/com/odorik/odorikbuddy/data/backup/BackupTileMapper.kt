package com.odorik.odorikbuddy.data.backup

import com.odorik.odorikbuddy.data.local.entity.TileCallType
import com.odorik.odorikbuddy.data.local.entity.TileEntity

/** Converts tiles between the database and the backup file. Pure, unit-tested. */
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
        // widgetStyle is an unused column (see TileEntity) and is not exported.
    )

    /**
     * Backup tiles to database rows. Original ids are kept so tile widgets on the same phone
     * keep working; a missing, invalid or repeated id becomes 0 and Room assigns a new one.
     * Positions are renumbered 0..n-1 in the backup's order.
     * Expects tiles already validated by [BackupCodec.decode] (label and recipient present).
     */
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

package com.odorik.odorikbuddy.data.backup

import com.odorik.odorikbuddy.data.local.entity.TileCallType
import com.odorik.odorikbuddy.data.local.entity.TileEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupTileMapperTest {

    private fun backupTile(id: Int?, position: Int?, label: String = "T$id") =
        BackupTile(id = id, position = position, label = label, recipient = "123", callType = "CALLBACK")

    @Test
    fun entityRoundTripKeepsFields() {
        val entity = TileEntity(
            id = 7, position = 0, label = "Babička", recipient = "+420777123456",
            callType = TileCallType.ONESHOT, lineId = "12345", callerId = "601000000",
            useLineAsCallerId = true, color = 0xFF4CAF50, textColor = 0xFFFFFFFF
        )
        assertEquals(listOf(entity), BackupTileMapper.toEntities(listOf(BackupTileMapper.toBackup(entity))))
    }

    @Test
    fun idsAreKeptAndDuplicatesOrInvalidBecomeNew() {
        val ids = BackupTileMapper.toEntities(
            listOf(backupTile(5, 0), backupTile(5, 1), backupTile(0, 2), backupTile(-1, 3), backupTile(null, 4))
        ).map { it.id }
        assertEquals(listOf(5, 0, 0, 0, 0), ids)
    }

    @Test
    fun positionsAreRenumberedInBackupOrder() {
        val tiles = BackupTileMapper.toEntities(
            listOf(backupTile(1, 10, "C"), backupTile(2, 3, "A"), backupTile(3, null, "D"), backupTile(4, 3, "B"))
        )
        assertEquals(listOf("A", "B", "C", "D"), tiles.map { it.label })
        assertEquals(listOf(0, 1, 2, 3), tiles.map { it.position })
    }

    @Test
    fun unknownCallTypeFallsBackLikeTheDatabase() {
        val tile = BackupTileMapper.toEntities(listOf(backupTile(1, 0).copy(callType = "WHAT"))).single()
        assertEquals(TileCallType.ONESHOT, tile.callType)
    }

    @Test
    fun tabOrderValidation() {
        assertTrue(SettingsBackupRepository.isValidTabOrder("tiles_title,callback_title,oneshot_call"))
        assertTrue(SettingsBackupRepository.isValidTabOrder("callback_title"))
        assertFalse(SettingsBackupRepository.isValidTabOrder(""))
        assertFalse(SettingsBackupRepository.isValidTabOrder("tiles_title,tiles_title"))
        assertFalse(SettingsBackupRepository.isValidTabOrder("tiles_title,hacked"))
    }
}

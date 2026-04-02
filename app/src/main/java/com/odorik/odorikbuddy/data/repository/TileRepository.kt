package com.odorik.odorikbuddy.data.repository

import com.odorik.odorikbuddy.data.local.TileDao
import com.odorik.odorikbuddy.data.local.entity.TileEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TileRepository @Inject constructor(
    private val tileDao: TileDao
) {
    fun getAllTiles(): Flow<List<TileEntity>> = tileDao.getAllTiles()

    suspend fun getTileById(id: Int): TileEntity? = tileDao.getTileById(id)

    suspend fun insertTile(tile: TileEntity) = tileDao.insertTile(tile)

    suspend fun updateTile(tile: TileEntity) = tileDao.updateTile(tile)

    suspend fun deleteTile(tile: TileEntity) = tileDao.deleteTile(tile)

    suspend fun updateTilePosition(tileId: Int, newPosition: Int) = tileDao.updateTilePosition(tileId, newPosition)

    suspend fun updateTiles(tiles: List<TileEntity>) = tileDao.updateTiles(tiles)
}

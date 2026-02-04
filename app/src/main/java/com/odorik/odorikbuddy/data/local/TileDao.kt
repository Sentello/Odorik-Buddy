package com.odorik.odorikbuddy.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.odorik.odorikbuddy.data.local.entity.TileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TileDao {
    @Query("SELECT * FROM tiles ORDER BY position ASC")
    fun getAllTiles(): Flow<List<TileEntity>>

    @Query("SELECT * FROM tiles WHERE id = :id")
    suspend fun getTileById(id: Int): TileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTile(tile: TileEntity)

    @Update
    suspend fun updateTile(tile: TileEntity)

    @Delete
    suspend fun deleteTile(tile: TileEntity)

    @Query("UPDATE tiles SET position = :newPosition WHERE id = :tileId")
    suspend fun updateTilePosition(tileId: Int, newPosition: Int)
    
    @Update
    suspend fun updateTiles(tiles: List<TileEntity>)
}

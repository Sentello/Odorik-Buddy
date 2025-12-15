package com.odorik.odorikbuddy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.odorik.odorikbuddy.model.HistoryItem

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(items: List<HistoryItem>)

    @Query("SELECT * FROM history ORDER BY date DESC")
    suspend fun getAllHistory(): List<HistoryItem>

    @Query("DELETE FROM history")
    suspend fun clearHistory()
}
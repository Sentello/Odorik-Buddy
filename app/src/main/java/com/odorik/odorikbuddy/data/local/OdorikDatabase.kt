package com.odorik.odorikbuddy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.odorik.odorikbuddy.model.HistoryItem

@Database(
    entities = [HistoryItem::class],
    version = 4,
    exportSchema = false
)
abstract class OdorikDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE history ADD COLUMN ringing_length INTEGER")
                db.execSQL("ALTER TABLE history ADD COLUMN destination_name TEXT")
                db.execSQL("ALTER TABLE history ADD COLUMN redirection_parent_id TEXT")
            }
        }
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE history ADD COLUMN line INTEGER")
            }
        }
        
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE history ADD COLUMN price_per_minute REAL")
                db.execSQL("ALTER TABLE history ADD COLUMN recording TEXT")
            }
        }
    }
}
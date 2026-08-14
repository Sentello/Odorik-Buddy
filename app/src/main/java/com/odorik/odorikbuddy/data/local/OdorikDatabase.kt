package com.odorik.odorikbuddy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.odorik.odorikbuddy.data.local.entity.TileEntity
import com.odorik.odorikbuddy.model.HistoryItem

@Database(
    entities = [HistoryItem::class, TileEntity::class],
    version = 8,
    exportSchema = true
)
abstract class OdorikDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun tileDao(): TileDao

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

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `tiles` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `position` INTEGER NOT NULL, 
                        `label` TEXT NOT NULL, 
                        `recipient` TEXT NOT NULL, 
                        `callType` TEXT NOT NULL, 
                        `lineId` TEXT, 
                        `callerId` TEXT, 
                        `useLineAsCallerId` INTEGER NOT NULL, 
                        `color` INTEGER
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tiles ADD COLUMN textColor INTEGER")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tiles ADD COLUMN widgetStyle TEXT NOT NULL DEFAULT 'SQUARE'")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE history ADD COLUMN event_type TEXT")
            }
        }
    }
}
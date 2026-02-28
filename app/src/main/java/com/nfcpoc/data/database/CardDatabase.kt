package com.nfcpoc.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nfcpoc.data.model.NfcCard

/**
 * Room database singleton for the NFC PoC application.
 *
 * - Version 1 — initial schema.
 * - Migrations must be added here as the schema evolves.
 */
@Database(
    entities = [NfcCard::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CardDatabase : RoomDatabase() {

    abstract fun cardDao(): CardDao

    companion object {
        private const val DATABASE_NAME = "nfc_poc_db"

        @Volatile
        private var INSTANCE: CardDatabase? = null

        fun getInstance(context: Context): CardDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): CardDatabase {
            return Room.databaseBuilder(
                context,
                CardDatabase::class.java,
                DATABASE_NAME
            )
                // fallbackToDestructiveMigration only for dev; remove in production
                // and supply explicit Migration objects instead.
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}

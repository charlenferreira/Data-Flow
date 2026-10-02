package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DailyUsageEntity::class, SpeedTestEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DataPulseDatabase : RoomDatabase() {

    abstract fun dataPulseDao(): DataPulseDao

    companion object {
        @Volatile
        private var INSTANCE: DataPulseDatabase? = null

        fun getDatabase(context: Context): DataPulseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DataPulseDatabase::class.java,
                    "datapulse_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

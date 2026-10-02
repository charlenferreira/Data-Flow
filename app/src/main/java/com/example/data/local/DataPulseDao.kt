package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DataPulseDao {

    @Query("SELECT * FROM daily_usage ORDER BY date DESC LIMIT 30")
    fun getRecentUsage(): Flow<List<DailyUsageEntity>>

    @Query("SELECT * FROM daily_usage WHERE date = :date LIMIT 1")
    suspend fun getUsageForDate(date: String): DailyUsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyUsage(usage: DailyUsageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageList(list: List<DailyUsageEntity>)

    @Query("SELECT * FROM speed_tests ORDER BY timestamp DESC LIMIT 20")
    fun getRecentSpeedTests(): Flow<List<SpeedTestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeedTest(test: SpeedTestEntity)

    @Query("DELETE FROM speed_tests WHERE id = :id")
    suspend fun deleteSpeedTestById(id: Int)

    @Query("DELETE FROM speed_tests")
    suspend fun clearSpeedTests()
}

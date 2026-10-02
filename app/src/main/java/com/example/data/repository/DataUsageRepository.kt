package com.example.data.repository

import com.example.data.local.DailyUsageEntity
import com.example.data.local.DataPulseDao
import com.example.data.local.SpeedTestEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DataUsageRepository(private val dao: DataPulseDao) {

    val recentUsage: Flow<List<DailyUsageEntity>> = dao.getRecentUsage()
    val recentSpeedTests: Flow<List<SpeedTestEntity>> = dao.getRecentSpeedTests()

    suspend fun saveDailyUsage(entity: DailyUsageEntity) {
        dao.insertOrUpdateDailyUsage(entity)
    }

    suspend fun saveSpeedTest(entity: SpeedTestEntity) {
        dao.insertSpeedTest(entity)
    }

    suspend fun deleteSpeedTest(id: Int) {
        dao.deleteSpeedTestById(id)
    }

    suspend fun seedInitialHistoryIfNeeded() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        val sampleDays = listOf(
            Triple(4.8f, 12.4f, 1.2f),
            Triple(6.2f, 15.1f, 0.8f),
            Triple(3.9f, 9.8f, 0.4f),
            Triple(7.4f, 18.2f, 2.1f),
            Triple(5.1f, 11.5f, 0.5f),
            Triple(8.3f, 22.0f, 1.9f),
            Triple(6.7f, 14.3f, 1.1f)
        )

        val list = mutableListOf<DailyUsageEntity>()
        for (i in sampleDays.indices) {
            val dateCal = Calendar.getInstance()
            dateCal.add(Calendar.DAY_OF_YEAR, -(6 - i))
            val dateStr = dateFormat.format(dateCal.time)

            val existing = dao.getUsageForDate(dateStr)
            if (existing == null) {
                val (mobGb, wifiGb, hotGb) = sampleDays[i]
                list.add(
                    DailyUsageEntity(
                        date = dateStr,
                        mobileBytes = (mobGb * 1024L * 1024L * 1024L).toLong(),
                        wifiBytes = (wifiGb * 1024L * 1024L * 1024L).toLong(),
                        hotspotBytes = (hotGb * 1024L * 1024L * 1024L).toLong(),
                        averageSpeedMbps = 85.0f + (i * 4f),
                        timestamp = dateCal.timeInMillis
                    )
                )
            }
        }

        if (list.isNotEmpty()) {
            dao.insertUsageList(list)
        }
    }
}

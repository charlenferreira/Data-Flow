package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_usage")
data class DailyUsageEntity(
    @PrimaryKey
    val date: String, // Format: YYYY-MM-DD
    val mobileBytes: Long,
    val wifiBytes: Long,
    val hotspotBytes: Long,
    val averageSpeedMbps: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

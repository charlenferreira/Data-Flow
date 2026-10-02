package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speed_tests")
data class SpeedTestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val pingMs: Int,
    val downloadMbps: Float,
    val uploadMbps: Float,
    val networkType: String,
    val rating: String,
    val isThrottledSuspected: Boolean = false
)

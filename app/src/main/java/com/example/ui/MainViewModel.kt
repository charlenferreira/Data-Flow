package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DailyUsageEntity
import com.example.data.local.DataPulseDatabase
import com.example.data.local.SpeedTestEntity
import com.example.data.repository.DataUsageRepository
import com.example.model.AppCategory
import com.example.model.AppUsageItem
import com.example.model.NetworkStatus
import com.example.model.OperatorPreset
import com.example.model.PlanSettings
import com.example.model.SpeedTestLiveState
import com.example.network.NetworkStatsHelper
import com.example.network.SpeedTestManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = DataPulseDatabase.getDatabase(context)
    private val repository = DataUsageRepository(database.dataPulseDao())
    private val networkStatsHelper = NetworkStatsHelper(context)
    private val speedTestManager = SpeedTestManager()

    // Plan Configuration
    private val _planSettings = MutableStateFlow(PlanSettings())
    val planSettings: StateFlow<PlanSettings> = _planSettings.asStateFlow()

    // Live Network Status
    private val _networkStatus = MutableStateFlow(networkStatsHelper.getCurrentNetworkStatus())
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    // Permission state
    private val _hasUsagePermission = MutableStateFlow(networkStatsHelper.hasUsageStatsPermission())
    val hasUsagePermission: StateFlow<Boolean> = _hasUsagePermission.asStateFlow()

    // App Usage List
    private val _allAppUsage = MutableStateFlow<List<AppUsageItem>>(emptyList())
    val allAppUsage: StateFlow<List<AppUsageItem>> = _allAppUsage.asStateFlow()

    // Filter states
    private val _selectedCategoryFilter = MutableStateFlow<AppCategory?>(null)
    val selectedCategoryFilter: StateFlow<AppCategory?> = _selectedCategoryFilter.asStateFlow()

    private val _filterOnlyBatteryDrain = MutableStateFlow(false)
    val filterOnlyBatteryDrain: StateFlow<Boolean> = _filterOnlyBatteryDrain.asStateFlow()

    // Speed test state
    private val _speedTestLiveState = MutableStateFlow(SpeedTestLiveState())
    val speedTestLiveState: StateFlow<SpeedTestLiveState> = _speedTestLiveState.asStateFlow()

    // History flows from Room
    val dailyUsageHistory: StateFlow<List<DailyUsageEntity>> = repository.recentUsage
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val speedTestHistory: StateFlow<List<SpeedTestEntity>> = repository.recentSpeedTests
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current month cycle calculated usage
    private val _cycleUsedMobileGB = MutableStateFlow(34.2f)
    val cycleUsedMobileGB: StateFlow<Float> = _cycleUsedMobileGB.asStateFlow()

    private val _todayUsedMobileGB = MutableStateFlow(4.8f)
    val todayUsedMobileGB: StateFlow<Float> = _todayUsedMobileGB.asStateFlow()

    private val _hotspotUsedGB = MutableStateFlow(4.35f)
    val hotspotUsedGB: StateFlow<Float> = _hotspotUsedGB.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialHistoryIfNeeded()
            refreshData()
        }
    }

    fun refreshData() {
        val permitted = networkStatsHelper.hasUsageStatsPermission()
        _hasUsagePermission.value = permitted
        _networkStatus.value = networkStatsHelper.getCurrentNetworkStatus()

        val apps = networkStatsHelper.getAppUsageList(_planSettings.value.billingCycleResetDay)
        _allAppUsage.value = apps

        // Compute total cycle mobile data from apps
        val totalBytes = apps.sumOf { it.totalBytes }
        if (totalBytes > 0) {
            val totalGB = (totalBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)).toFloat()
            _cycleUsedMobileGB.value = totalGB
        }

        // Hotspot usage calculation
        val hotspotApp = apps.find { it.packageName.contains("system") || it.appName.contains("Hotspot") || it.appName.contains("Roteador") }
        if (hotspotApp != null) {
            _hotspotUsedGB.value = (hotspotApp.totalBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)).toFloat()
        }
    }

    fun updatePlanSettings(newSettings: PlanSettings) {
        _planSettings.value = newSettings
    }

    fun selectPreset(preset: OperatorPreset) {
        _planSettings.value = _planSettings.value.copy(
            carrierName = preset.title,
            highSpeedFupLimitGB = preset.defaultFupGB,
            hotspotLimitGB = preset.defaultHotspotGB
        )
    }

    fun setCategoryFilter(category: AppCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun toggleBatteryDrainFilter() {
        _filterOnlyBatteryDrain.value = !_filterOnlyBatteryDrain.value
    }

    fun startSpeedTest() {
        if (_speedTestLiveState.value.isRunning) return

        viewModelScope.launch {
            speedTestManager.runSpeedTest().collect { state ->
                _speedTestLiveState.value = state

                if (state.phase == com.example.model.SpeedTestPhase.FINISHED) {
                    val entity = SpeedTestEntity(
                        timestamp = System.currentTimeMillis(),
                        pingMs = state.pingMs,
                        downloadMbps = state.finalDownloadMbps,
                        uploadMbps = state.finalUploadMbps,
                        networkType = _networkStatus.value.connectionType.shortBadge,
                        rating = state.qualityVerdict,
                        isThrottledSuspected = state.throttleRiskDetected
                    )
                    repository.saveSpeedTest(entity)
                }
            }
        }
    }

    fun deleteSpeedTest(id: Int) {
        viewModelScope.launch {
            repository.deleteSpeedTest(id)
        }
    }
}

package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
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
import com.example.network.AppReleaseInfo
import com.example.network.DeviceDataSummary
import com.example.network.NetworkStatsHelper
import com.example.network.SpeedTestManager
import com.example.network.UpdateChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UpdateUiState {
    object Idle : UpdateUiState()
    object Checking : UpdateUiState()
    data class UpdateAvailable(val releaseInfo: AppReleaseInfo) : UpdateUiState()
    data class UpToDate(val currentVersion: String, val manualCheck: Boolean) : UpdateUiState()
    data class Error(val message: String, val manualCheck: Boolean) : UpdateUiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = DataPulseDatabase.getDatabase(context)
    private val repository = DataUsageRepository(database.dataPulseDao())
    private val networkStatsHelper = NetworkStatsHelper(context)
    private val speedTestManager = SpeedTestManager()
    private val updateChecker = UpdateChecker()

    // Plan Configuration
    private val _planSettings = MutableStateFlow(PlanSettings())
    val planSettings: StateFlow<PlanSettings> = _planSettings.asStateFlow()

    // Live Network Status
    private val _networkStatus = MutableStateFlow(networkStatsHelper.getCurrentNetworkStatus())
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    // Permission state
    private val _hasUsagePermission = MutableStateFlow(networkStatsHelper.hasUsageStatsPermission())
    val hasUsagePermission: StateFlow<Boolean> = _hasUsagePermission.asStateFlow()

    private val _showUsagePermissionDialog = MutableStateFlow(!networkStatsHelper.hasUsageStatsPermission())
    val showUsagePermissionDialog: StateFlow<Boolean> = _showUsagePermissionDialog.asStateFlow()

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

    // Update checker state
    private val _updateState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val updateState: StateFlow<UpdateUiState> = _updateState.asStateFlow()

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

    private val _daysRemainingInCycle = MutableStateFlow(12)
    val daysRemainingInCycle: StateFlow<Int> = _daysRemainingInCycle.asStateFlow()

    private val _recommendedDailyGB = MutableStateFlow(1.3f)
    val recommendedDailyGB: StateFlow<Float> = _recommendedDailyGB.asStateFlow()

    private val _deviceUsageSummary = MutableStateFlow(DeviceDataSummary(0L, 0L, 0L, 0L))
    val deviceUsageSummary: StateFlow<DeviceDataSummary> = _deviceUsageSummary.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialHistoryIfNeeded()
            refreshData()
            checkForUpdates(manual = false)
        }
    }

    fun calculateCycleProjections() {
        val cal = java.util.Calendar.getInstance()
        val today = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val maxDays = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        val resetDay = _planSettings.value.billingCycleResetDay

        val daysLeft = if (today < resetDay) {
            resetDay - today
        } else {
            (maxDays - today) + resetDay
        }.coerceAtLeast(1)

        _daysRemainingInCycle.value = daysLeft

        val remainingGB = (_planSettings.value.highSpeedFupLimitGB - _cycleUsedMobileGB.value).coerceAtLeast(0f)
        _recommendedDailyGB.value = remainingGB / daysLeft
    }

    fun refreshData() {
        val permitted = networkStatsHelper.hasUsageStatsPermission()
        _hasUsagePermission.value = permitted
        _networkStatus.value = networkStatsHelper.getCurrentNetworkStatus()

        val (cycleStart, now) = networkStatsHelper.getBillingCycleTimes(_planSettings.value.billingCycleResetDay)
        val deviceSummary = networkStatsHelper.getDeviceTotalUsage(cycleStart, now)
        _deviceUsageSummary.value = deviceSummary

        val apps = networkStatsHelper.getAppUsageList(_planSettings.value.billingCycleResetDay)
        _allAppUsage.value = apps

        // Compute total cycle mobile data
        val totalBytes = if (deviceSummary.totalMobileBytes > 0) {
            deviceSummary.totalMobileBytes
        } else {
            apps.sumOf { it.totalBytes }
        }

        if (totalBytes > 0) {
            val totalGB = (totalBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)).toFloat()
            _cycleUsedMobileGB.value = totalGB
        }

        // Hotspot usage calculation
        val hotspotApp = apps.find { it.packageName.contains("system") || it.appName.contains("Hotspot") || it.appName.contains("Roteador") }
        if (hotspotApp != null) {
            _hotspotUsedGB.value = (hotspotApp.totalBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)).toFloat()
        }
        calculateCycleProjections()
    }

    fun updatePlanSettings(newSettings: PlanSettings) {
        _planSettings.value = newSettings
        calculateCycleProjections()
    }

    fun selectPreset(preset: OperatorPreset) {
        _planSettings.value = _planSettings.value.copy(
            carrierName = preset.title,
            highSpeedFupLimitGB = preset.defaultFupGB,
            hotspotLimitGB = preset.defaultHotspotGB
        )
        calculateCycleProjections()
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

    fun checkForUpdates(manual: Boolean = true) {
        viewModelScope.launch {
            _updateState.value = UpdateUiState.Checking
            val result = updateChecker.checkForUpdates()
            result.onSuccess { info ->
                if (info.isNewerVersion) {
                    _updateState.value = UpdateUiState.UpdateAvailable(info)
                } else {
                    _updateState.value = UpdateUiState.UpToDate(BuildConfig.VERSION_NAME, manual)
                }
            }.onFailure { err ->
                _updateState.value = UpdateUiState.Error(err.message ?: "Não foi possível verificar atualizações.", manual)
            }
        }
    }

    fun dismissUpdateDialog() {
        _updateState.value = UpdateUiState.Idle
    }

    fun dismissUsagePermissionDialog() {
        _showUsagePermissionDialog.value = false
    }

    fun requestUsagePermissionDialog() {
        _showUsagePermissionDialog.value = true
    }
}

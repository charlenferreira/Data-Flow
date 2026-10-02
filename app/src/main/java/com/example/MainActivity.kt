package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.screens.AppsUsageScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.PlanSettingsScreen
import com.example.ui.screens.SpeedDiagnosticScreen
import com.example.ui.theme.CardDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan

enum class AppScreen(val title: String, val icon: ImageVector, val tag: String) {
    OVERVIEW("Visão Geral", Icons.Default.Dashboard, "tab_overview"),
    APPS("Apps", Icons.Default.Apps, "tab_apps"),
    SPEED("Diagnóstico", Icons.Default.Speed, "tab_speed"),
    SETTINGS("Meu Plano", Icons.Default.Tune, "tab_settings")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer()
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: MainViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf(AppScreen.OVERVIEW) }

    val planSettings by viewModel.planSettings.collectAsStateWithLifecycle()
    val networkStatus by viewModel.networkStatus.collectAsStateWithLifecycle()
    val hasUsagePermission by viewModel.hasUsagePermission.collectAsStateWithLifecycle()
    val appUsageList by viewModel.allAppUsage.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val filterOnlyBatteryDrain by viewModel.filterOnlyBatteryDrain.collectAsStateWithLifecycle()
    val speedLiveState by viewModel.speedTestLiveState.collectAsStateWithLifecycle()
    val dailyUsageHistory by viewModel.dailyUsageHistory.collectAsStateWithLifecycle()
    val speedHistory by viewModel.speedTestHistory.collectAsStateWithLifecycle()
    val cycleUsedGB by viewModel.cycleUsedMobileGB.collectAsStateWithLifecycle()
    val todayUsedGB by viewModel.todayUsedMobileGB.collectAsStateWithLifecycle()
    val hotspotUsedGB by viewModel.hotspotUsedGB.collectAsStateWithLifecycle()

    // Handle back button on sub-screens
    if (currentScreen != AppScreen.OVERVIEW) {
        BackHandler {
            currentScreen = AppScreen.OVERVIEW
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = CardDark,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                AppScreen.entries.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(screen.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            AppScreen.OVERVIEW -> {
                OverviewScreen(
                    planSettings = planSettings,
                    networkStatus = networkStatus,
                    cycleUsedGB = cycleUsedGB,
                    todayUsedGB = todayUsedGB,
                    hotspotUsedGB = hotspotUsedGB,
                    dailyUsageList = dailyUsageHistory,
                    onNavigateToApps = { currentScreen = AppScreen.APPS },
                    onNavigateToSpeed = { currentScreen = AppScreen.SPEED },
                    onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppScreen.APPS -> {
                AppsUsageScreen(
                    apps = appUsageList,
                    hasUsagePermission = hasUsagePermission,
                    selectedCategory = selectedCategory,
                    filterOnlyBatteryDrain = filterOnlyBatteryDrain,
                    onCategorySelected = { viewModel.setCategoryFilter(it) },
                    onToggleBatteryFilter = { viewModel.toggleBatteryDrainFilter() },
                    onRefresh = { viewModel.refreshData() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppScreen.SPEED -> {
                SpeedDiagnosticScreen(
                    networkStatus = networkStatus,
                    speedState = speedLiveState,
                    history = speedHistory,
                    onStartTest = { viewModel.startSpeedTest() },
                    onDeleteHistoryItem = { viewModel.deleteSpeedTest(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppScreen.SETTINGS -> {
                PlanSettingsScreen(
                    planSettings = planSettings,
                    onUpdatePlanSettings = { viewModel.updatePlanSettings(it) },
                    onSelectPreset = { viewModel.selectPreset(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

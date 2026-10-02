package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.UpdateUiState
import com.example.ui.screens.AppsUsageScreen
import com.example.ui.screens.BatteryScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.PlanSettingsScreen
import com.example.ui.screens.SpeedDiagnosticScreen
import com.example.ui.theme.CardDark
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan

enum class AppScreen(val title: String, val icon: ImageVector, val tag: String) {
    OVERVIEW("Geral", Icons.Default.Dashboard, "tab_overview"),
    BATTERY("Bateria", Icons.Default.BatteryChargingFull, "tab_battery"),
    APPS("Rede & Apps", Icons.Default.Apps, "tab_apps"),
    SPEED("Diagnóstico", Icons.Default.Speed, "tab_speed"),
    SETTINGS("Sistema", Icons.Default.Tune, "tab_settings")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.OVERVIEW) }

    val planSettings by viewModel.planSettings.collectAsStateWithLifecycle()
    val networkStatus by viewModel.networkStatus.collectAsStateWithLifecycle()
    val hasUsagePermission by viewModel.hasUsagePermission.collectAsStateWithLifecycle()
    val showUsagePermissionDialog by viewModel.showUsagePermissionDialog.collectAsStateWithLifecycle()
    val appUsageList by viewModel.allAppUsage.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val filterOnlyBatteryDrain by viewModel.filterOnlyBatteryDrain.collectAsStateWithLifecycle()
    val speedLiveState by viewModel.speedTestLiveState.collectAsStateWithLifecycle()
    val dailyUsageHistory by viewModel.dailyUsageHistory.collectAsStateWithLifecycle()
    val speedHistory by viewModel.speedTestHistory.collectAsStateWithLifecycle()
    val cycleUsedGB by viewModel.cycleUsedMobileGB.collectAsStateWithLifecycle()
    val todayUsedGB by viewModel.todayUsedMobileGB.collectAsStateWithLifecycle()
    val hotspotUsedGB by viewModel.hotspotUsedGB.collectAsStateWithLifecycle()
    val daysRemaining by viewModel.daysRemainingInCycle.collectAsStateWithLifecycle()
    val recommendedDailyGB by viewModel.recommendedDailyGB.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val batteryTelemetry by viewModel.batteryTelemetry.collectAsStateWithLifecycle()

    // Handle back button on sub-screens
    if (currentScreen != AppScreen.OVERVIEW) {
        BackHandler {
            currentScreen = AppScreen.OVERVIEW
        }
    }

    // Material 3 Dialog: Usage Stats Permission Guidance
    if (showUsagePermissionDialog && !hasUsagePermission) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissUsagePermissionDialog() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Acesso ao Uso de Dados",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Para quantificar os bytes reais enviados e recebidos por cada aplicativo instalado (Wi-Fi e Dados Móveis) e identificar os maiores consumidores em segundo plano, o Data Pulse precisa da permissão de Acesso ao Uso.\n\nToque em 'Abrir Configurações' e autorize o Data Pulse na lista do Android.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissUsagePermissionDialog()
                        try {
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = DeepNavy
                    ),
                    modifier = Modifier.testTag("dialog_grant_usage_permission_button")
                ) {
                    Text("Abrir Configurações", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissUsagePermissionDialog() }) {
                    Text("Agora Não")
                }
            }
        )
    }

    // Material 3 Dialog: GitHub Update Available
    when (val state = updateState) {
        is UpdateUiState.UpdateAvailable -> {
            val info = state.releaseInfo
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = {
                    Text(
                        text = "Nova Versão Disponível (${info.tagName})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = "Uma versão mais recente do Data Pulse foi publicada no GitHub oficial.\n\n" +
                                "• Versão atual: v${BuildConfig.VERSION_NAME}\n" +
                                "• Nova versão: ${info.tagName}\n\n" +
                                "Notas da versão:\n${info.changelog}",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.dismissUpdateDialog()
                            val downloadUrl = info.directApkDownloadUrl ?: info.releasePageUrl
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = DeepNavy
                        ),
                        modifier = Modifier.testTag("download_update_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Baixar Nova Versão", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                        Text("Depois")
                    }
                }
            )
        }

        is UpdateUiState.UpToDate -> {
            if (state.manualCheck) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissUpdateDialog() },
                    title = { Text("Aplicativo Atualizado", fontWeight = FontWeight.Bold) },
                    text = {
                        Text("Você já está utilizando a versão mais recente do Data Pulse (v${state.currentVersion}).")
                    },
                    confirmButton = {
                        Button(onClick = { viewModel.dismissUpdateDialog() }) {
                            Text("OK")
                        }
                    }
                )
            }
        }

        is UpdateUiState.Error -> {
            if (state.manualCheck) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissUpdateDialog() },
                    title = { Text("Verificação de Atualização", fontWeight = FontWeight.Bold) },
                    text = { Text(state.message) },
                    confirmButton = {
                        Button(onClick = { viewModel.dismissUpdateDialog() }) {
                            Text("OK")
                        }
                    }
                )
            }
        }

        else -> {}
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Data Flow",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.checkForUpdates(manual = true) },
                        modifier = Modifier.testTag("check_updates_topbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = "Verificar Atualizações no GitHub",
                            tint = NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CardDark,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
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
                    daysRemaining = daysRemaining,
                    recommendedDailyGB = recommendedDailyGB,
                    dailyUsageList = dailyUsageHistory,
                    batteryTelemetry = batteryTelemetry,
                    onNavigateToApps = { currentScreen = AppScreen.APPS },
                    onNavigateToSpeed = { currentScreen = AppScreen.SPEED },
                    onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                    onNavigateToBattery = { currentScreen = AppScreen.BATTERY },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppScreen.BATTERY -> {
                BatteryScreen(
                    telemetry = batteryTelemetry,
                    onToggleAlarm = { viewModel.toggleBatteryAlarm(it) },
                    onThresholdChange = { viewModel.setBatteryAlarmThreshold(it) },
                    onRefresh = { viewModel.refreshBatteryTelemetry() },
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
                    onCheckUpdates = { viewModel.checkForUpdates(manual = true) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

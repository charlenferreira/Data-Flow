package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DailyUsageEntity
import com.example.model.BatteryTelemetry
import com.example.model.NetworkStatus
import com.example.model.PlanSettings
import com.example.ui.components.CircularDataGauge
import com.example.ui.components.UsageBarChart
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DataSaverOn
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.ui.theme.CyberIndigo
import com.example.ui.theme.DangerRose
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.WarningAmber

@Composable
fun OverviewScreen(
    planSettings: PlanSettings,
    networkStatus: NetworkStatus,
    cycleUsedGB: Float,
    todayUsedGB: Float,
    hotspotUsedGB: Float,
    daysRemaining: Int = 12,
    recommendedDailyGB: Float = 1.3f,
    dailyUsageList: List<DailyUsageEntity>,
    batteryTelemetry: BatteryTelemetry? = null,
    onNavigateToApps: () -> Unit,
    onNavigateToSpeed: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToBattery: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showUnlimitedWhyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Header with Active Network Badge & Unlimited explanation toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DataPulse",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = planSettings.carrierName,
                    fontSize = 13.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (networkStatus.isConnected) MintEmerald.copy(alpha = 0.15f) else DangerRose.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (networkStatus.isConnected) MintEmerald.copy(alpha = 0.4f) else DangerRose.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (networkStatus.isConnected) MintEmerald else DangerRose)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = networkStatus.connectionType.shortBadge,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (networkStatus.isConnected) MintEmerald else DangerRose
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // AccuBattery Status Summary Card
        if (batteryTelemetry != null) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorderDark, RoundedCornerShape(18.dp))
                    .clickable { onNavigateToBattery() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (batteryTelemetry.isCharging) EmeraldGreen.copy(alpha = 0.15f) else NeonCyan.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (batteryTelemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = if (batteryTelemetry.isCharging) EmeraldGreen else NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Bateria: ${batteryTelemetry.levelPercent}%",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (batteryTelemetry.isCharging) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = EmeraldGreen.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "CARREGANDO",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${batteryTelemetry.currentNowMa} mA • ${String.format("%.1f", batteryTelemetry.temperatureCelsius)}°C • ${batteryTelemetry.voltageVolts}V",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onNavigateToBattery,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan.copy(alpha = 0.15f),
                            contentColor = NeonCyan
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Detalhes", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // "Por que gerenciar internet ilimitada?" Callout Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showUnlimitedWhyDialog = !showUnlimitedWhyDialog }
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Por que gerenciar?",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Por que gerenciar se o plano é ilimitado?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = if (showUnlimitedWhyDialog) "Fechar" else "Ver motivos",
                        fontSize = 12.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.Medium
                    )
                }

                AnimatedVisibility(visible = showUnlimitedWhyDialog) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text(
                            text = "Mesmo em planos 'ilimitados', você deve monitorar por 4 razões essenciais:\n" +
                                    "1. Redução de Franquia (FUP): Operadoras cortam a velocidade para 512Kbps após o limite de alta velocidade.\n" +
                                    "2. Hotspot limitado: Roteador para outros aparelhos tem cota separada restrita.\n" +
                                    "3. Bateria e Aquecimento: 5G transmitindo sem parar drena a bateria até 3x mais rápido.\n" +
                                    "4. Aplicativos Vampiros: Apps em segundo plano podem enviar dados pessoais e consumir sua largura de banda.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Circular Gauge Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorderDark, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularDataGauge(
                    usedGB = cycleUsedGB,
                    limitGB = planSettings.highSpeedFupLimitGB,
                    isUnlimitedWithoutFup = !planSettings.hasFupLimit
                )

                Spacer(modifier = Modifier.height(14.dp))

                // FUP Warning Banner if close to limit
                val fupRatio = cycleUsedGB / planSettings.highSpeedFupLimitGB
                if (planSettings.hasFupLimit && fupRatio >= 0.75f) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (fupRatio >= 0.90f) DangerRose.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Aviso FUP",
                                tint = if (fupRatio >= 0.90f) DangerRose else WarningAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (fupRatio >= 1.0f)
                                    "Atenção: Limite de alta velocidade atingido! Sua conexão pode estar reduzida."
                                else
                                    "Alerta: Você consumiu ${(fupRatio * 100).toInt()}% da sua franquia de alta velocidade.",
                                fontSize = 11.sp,
                                color = if (fupRatio >= 0.90f) DangerRose else WarningAmber,
                                lineHeight = 15.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ciclo renova dia ${planSettings.billingCycleResetDay}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Ajustar Plano",
                        fontSize = 12.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onNavigateToSettings() }
                            .testTag("adjust_plan_button")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card Explicativo: O que acontece ao bater o limite?
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ele reduz a internet após bater o limite?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• O aplicativo NÃO reduz sua internet: apenas sua operadora tem o controle técnico para reduzir a velocidade na antena após o teto contratual (FUP).\n" +
                           "• Meta sugerida: use até ${String.format(java.util.Locale.US, "%.1f", recommendedDailyGB)} GB/dia para manter 5G rápido nos próximos $daysRemaining dias do ciclo.\n" +
                           "• Quer economizar dados ativamente? Ative a Economia de Dados nativa do Android abaixo.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        try {
                            val intent = Intent("android.settings.DATA_SAVER_SETTINGS")
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            try {
                                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_data_saver_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DataSaverOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Abrir Economizador de Dados do Android", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Secondary Metrics Grid (Hoje, Hotspot, Bateria 5G)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card Uso Hoje
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Uso Hoje",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", todayUsedGB)} GB",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pico: Streaming 4K",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Card Hotspot
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Roteador",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.WifiTethering,
                            contentDescription = "Hotspot",
                            tint = CyberIndigo,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", hotspotUsedGB)} GB",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberIndigo
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val hotspotRatio = (hotspotUsedGB / planSettings.hotspotLimitGB).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { hotspotRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (hotspotRatio > 0.8f) WarningAmber else CyberIndigo,
                        trackColor = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "de ${planSettings.hotspotLimitGB.toInt()} GB limite",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History Chart Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorderDark, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Histórico de Tráfego",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Últimos 7 dias",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                UsageBarChart(items = dailyUsageList)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Prompts (Quick Diagnose & Apps)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateToSpeed,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("overview_speed_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Testar Rede", fontSize = 13.sp)
            }

            OutlinedButton(
                onClick = onNavigateToApps,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("overview_apps_button")
            ) {
                Icon(
                    imageVector = Icons.Default.BatteryAlert,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Ver Apps", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

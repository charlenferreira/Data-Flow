package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatteryTelemetry
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CyberIndigo
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.WarningAmber
import kotlin.math.abs

@Composable
fun BatteryScreen(
    telemetry: BatteryTelemetry,
    onToggleAlarm: (Boolean) -> Unit,
    onThresholdChange: (Int) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val levelAnim by animateFloatAsState(
        targetValue = telemetry.levelPercent / 100f,
        animationSpec = tween(1000),
        label = "battery_level_anim"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Telemetria de Bateria",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Medições em tempo real estilo AccuBattery",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .background(CardDark, CircleShape)
                    .border(1.dp, CardBorderDark, CircleShape)
                    .testTag("refresh_battery_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Atualizar telemetria",
                    tint = NeonCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Live Gauge Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (telemetry.isCharging) NeonCyan.copy(alpha = 0.5f) else CardBorderDark, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (telemetry.isCharging) NeonCyan.copy(alpha = 0.15f) else DeepNavy
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (telemetry.isCharging) Icons.Default.Bolt else Icons.Default.Power,
                            contentDescription = null,
                            tint = if (telemetry.isCharging) NeonCyan else WarningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (telemetry.isCharging) "CARREGANDO (${telemetry.pluggedSource.label})" else telemetry.pluggedSource.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (telemetry.isCharging) NeonCyan else WarningAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Circular Progress Dial
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(170.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        strokeWidth = 14.dp
                    )
                    CircularProgressIndicator(
                        progress = { levelAnim },
                        modifier = Modifier.fillMaxSize(),
                        color = if (telemetry.isCharging) EmeraldGreen else if (telemetry.levelPercent <= 20) AlertRed else NeonCyan,
                        strokeWidth = 14.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${telemetry.levelPercent}%",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${telemetry.currentCapacityMah} / ${telemetry.designCapacityMah} mAh",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Real-time Current in mA (Hero metric)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DeepNavy)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Fluxo de Corrente Instantâneo",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${if (telemetry.currentNowMa > 0) "+" else ""}${telemetry.currentNowMa} mA",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (telemetry.currentNowMa > 0) EmeraldGreen else WarningAmber
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Potência Ativa",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("%.2f W", telemetry.powerWatts),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Rate and Remaining Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Velocidade: ${String.format("%.1f", telemetry.chargeRatePercentPerHour)} %/hora",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val hours = telemetry.estimatedMinutesRemaining / 60
                    val mins = telemetry.estimatedMinutesRemaining % 60
                    Text(
                        text = if (telemetry.isCharging) "Tempo restante: ${hours}h ${mins}m" else "Duração restante: ${hours}h ${mins}m",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = NeonCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Safe Charge Alarm 80% (AccuBattery feature)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorderDark, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Alarme de Carga Segura (${telemetry.chargeAlarmThreshold}%)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Preserva ciclos e reduz desgaste em até 67%",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = telemetry.chargeAlarmEnabled,
                        onCheckedChange = onToggleAlarm,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = DeepNavy
                        )
                    )
                }

                if (telemetry.chargeAlarmEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Limite do alarme sonoro: ${telemetry.chargeAlarmThreshold}%",
                        fontSize = 12.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = telemetry.chargeAlarmThreshold.toFloat(),
                        onValueChange = { onThresholdChange(it.toInt()) },
                        valueRange = 70f..95f,
                        steps = 4,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Telemetry Grid
        Text(
            text = "Diagnóstico do Hardware",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricTile(
                icon = Icons.Default.ElectricMeter,
                title = "Tensão Elétrica",
                value = "${telemetry.voltageMv} mV",
                subtitle = String.format("%.2f V", telemetry.voltageVolts),
                tint = ElectricBlue,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                icon = Icons.Default.DeviceThermostat,
                title = "Temperatura",
                value = String.format("%.1f °C", telemetry.temperatureCelsius),
                subtitle = if (telemetry.isTemperatureHigh) "Atenção: Calor" else "Temperatura Ideal",
                tint = if (telemetry.isTemperatureHigh) AlertRed else EmeraldGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricTile(
                icon = Icons.Default.CheckCircle,
                title = "Saúde da Célula",
                value = telemetry.health.label,
                subtitle = "Química: ${telemetry.technology}",
                tint = if (telemetry.health.isWarning) AlertRed else NeonCyan,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                icon = Icons.Default.Security,
                title = "Ciclo Estimado",
                value = "~214 Ciclos",
                subtitle = "Capacidade: 98.4%",
                tint = CyberIndigo,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AccuBattery Scientific Tips Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Como funciona o desgaste da bateria?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Baterias de íon/polímero de lítio sofrem o maior estresse químico nas extremidades (abaixo de 15% e acima de 85%). Carregar até 80% gera apenas 0.2 ciclos de desgaste contra 1.0 ciclo completo ao carregar até 100%, quadruplicando a vida útil do seu smartphone.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MetricTile(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = modifier.border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = tint
            )
        }
    }
}

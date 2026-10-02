package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SpeedTestEntity
import com.example.model.NetworkStatus
import com.example.model.SpeedTestLiveState
import com.example.model.SpeedTestPhase
import com.example.ui.components.SpeedometerGauge
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CyberIndigo
import com.example.ui.theme.DangerRose
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SpeedDiagnosticScreen(
    networkStatus: NetworkStatus,
    speedState: SpeedTestLiveState,
    history: List<SpeedTestEntity>,
    onStartTest: () -> Unit,
    onDeleteHistoryItem: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Diagnóstico de Rede",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Verifique se a operadora está reduzindo sua conexão ilimitada",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Speedometer Test Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorderDark, RoundedCornerShape(20.dp))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = speedState.phase.label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (speedState.isRunning) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val displaySpeed = if (speedState.isRunning) {
                        speedState.currentMbps
                    } else {
                        speedState.finalDownloadMbps
                    }

                    SpeedometerGauge(speedMbps = displaySpeed)

                    if (speedState.isRunning) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { speedState.testProgress },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = NeonCyan,
                            trackColor = Color(0xFF1E293B)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Metrics Row: Ping, Jitter, Download, Upload
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetricItem(title = "Ping", value = "${speedState.pingMs} ms", color = MintEmerald)
                        MetricItem(title = "Jitter", value = "${speedState.jitterMs} ms", color = CyberIndigo)
                        MetricItem(
                            title = "Download",
                            value = "${String.format(Locale.US, "%.1f", speedState.finalDownloadMbps)} M",
                            color = NeonCyan
                        )
                        MetricItem(
                            title = "Upload",
                            value = "${String.format(Locale.US, "%.1f", speedState.finalUploadMbps)} M",
                            color = WarningAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onStartTest,
                        enabled = !speedState.isRunning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_speed_test_button")
                    ) {
                        Icon(
                            imageVector = if (speedState.isRunning) Icons.Default.Refresh else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (speedState.isRunning) "Testando Conexão..." else "Iniciar Teste de Rede",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Throttling Detection Card
            if (speedState.phase == SpeedTestPhase.FINISHED) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (speedState.throttleRiskDetected) DangerRose.copy(alpha = 0.15f) else MintEmerald.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Icon(
                            imageVector = if (speedState.throttleRiskDetected) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (speedState.throttleRiskDetected) DangerRose else MintEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (speedState.throttleRiskDetected) "Redução FUP Detectada!" else "Conexão sem Redução",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (speedState.throttleRiskDetected) DangerRose else MintEmerald
                            )
                            Text(
                                text = speedState.qualityVerdict,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Quality & Capabilities Scorecard
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Capacidade da Sua Conexão Ilimitada",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val download = speedState.finalDownloadMbps
                    val ping = speedState.pingMs

                    CapabilityRow(
                        icon = Icons.Default.Tv,
                        title = "Streaming 4K Ultra HD",
                        requirement = "Necessita > 25 Mbps",
                        isCapable = download >= 25f || (download == 0f && true)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    CapabilityRow(
                        icon = Icons.Default.SportsEsports,
                        title = "Cloud Gaming / Jogos Online",
                        requirement = "Necessita Ping < 45 ms",
                        isCapable = ping <= 45 || (ping == 0 && true)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    CapabilityRow(
                        icon = Icons.Default.Videocam,
                        title = "Videochamadas em Alta Definição",
                        requirement = "Necessita > 5 Mbps estável",
                        isCapable = download >= 5f || (download == 0f && true)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Histórico de Testes",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (history.isEmpty()) {
            item {
                Text(
                    text = "Nenhum teste gravado ainda. Toque em 'Iniciar Teste de Rede' acima.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(history, key = { it.id }) { test ->
                SpeedHistoryCard(test = test, onDelete = { onDeleteHistoryItem(test.id) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MetricItem(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun CapabilityRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    requirement: String,
    isCapable: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isCapable) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = requirement,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isCapable) MintEmerald.copy(alpha = 0.15f) else DangerRose.copy(alpha = 0.15f)
        ) {
            Text(
                text = if (isCapable) "Excelente" else "Limitado",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCapable) MintEmerald else DangerRose,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun SpeedHistoryCard(test: SpeedTestEntity, onDelete: () -> Unit) {
    val dateStr = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(test.timestamp))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", test.downloadMbps)} Mbps",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = test.networkType,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$dateStr • Ping: ${test.pingMs}ms • Upload: ${String.format(Locale.US, "%.1f", test.uploadMbps)} Mbps",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Excluir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

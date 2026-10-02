package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DailyUsageEntity
import com.example.ui.theme.CyberIndigo
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCyan

@Composable
fun UsageBarChart(
    items: List<DailyUsageEntity>,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Coletando histórico diário...",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
        return
    }

    val displayItems = remember(items) {
        items.takeLast(7).reversed()
    }

    var selectedIndex by remember { mutableStateOf(displayItems.lastIndex) }
    val maxMobileBytes = remember(displayItems) {
        displayItems.maxOfOrNull { it.mobileBytes }?.coerceAtLeast(1024L * 1024L * 1024L) ?: 1024L
    }

    Column(modifier = modifier.fillMaxWidth()) {
        val selectedItem = displayItems.getOrNull(selectedIndex) ?: displayItems.last()
        val selectedGB = selectedItem.mobileBytes / (1024.0 * 1024.0 * 1024.0)
        val selectedWifiGB = selectedItem.wifiBytes / (1024.0 * 1024.0 * 1024.0)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Dia: ${selectedItem.date.takeLast(5)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${String.format(java.util.Locale.US, "%.2f", selectedGB)} GB Móvel",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "Wi-Fi: ${String.format(java.util.Locale.US, "%.1f", selectedWifiGB)} GB",
                    fontSize = 11.sp,
                    color = MintEmerald,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            displayItems.forEachIndexed { index, item ->
                val ratio = (item.mobileBytes.toFloat() / maxMobileBytes).coerceIn(0.12f, 1f)
                val animatedRatio by animateFloatAsState(
                    targetValue = ratio,
                    animationSpec = tween(durationMillis = 600 + index * 50),
                    label = "bar_ratio"
                )
                val isSelected = index == selectedIndex

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedIndex = index }
                ) {
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .fillMaxHeight(animatedRatio)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                if (isSelected) NeonCyan else CyberIndigo.copy(alpha = 0.5f)
                            )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.date.takeLast(2),
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(NeonCyan)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Consumo Dados Móveis (Últimos 7 dias)",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

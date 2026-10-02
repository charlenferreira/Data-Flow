package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DangerRose
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.WarningAmber

@Composable
fun CircularDataGauge(
    usedGB: Float,
    limitGB: Float,
    isUnlimitedWithoutFup: Boolean = false,
    size: Dp = 210.dp,
    strokeWidth: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    val progress = if (isUnlimitedWithoutFup || limitGB <= 0) {
        0.35f
    } else {
        (usedGB / limitGB).coerceIn(0f, 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "gauge_progress"
    )

    val gaugeColor = when {
        isUnlimitedWithoutFup -> MintEmerald
        progress >= 0.90f -> DangerRose
        progress >= 0.75f -> WarningAmber
        else -> NeonCyan
    }

    val remainingGB = (limitGB - usedGB).coerceAtLeast(0f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val arcSize = this.size.minDimension - stroke

            // Track background arc (260 degrees)
            drawArc(
                color = Color(0xFF1E293B),
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Active progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(NeonCyan, gaugeColor, gaugeColor)
                ),
                startAngle = 140f,
                sweepAngle = 260f * animatedProgress,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = gaugeColor.copy(alpha = 0.15f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isUnlimitedWithoutFup) "ILIMITADO TOTAL" else "ALTA VELOCIDADE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = gaugeColor,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = String.format(java.util.Locale.US, "%.1f", usedGB),
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = if (isUnlimitedWithoutFup) "GB consumidos no mês" else "de ${limitGB.toInt()} GB (FUP)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isUnlimitedWithoutFup) {
                    "Sem risco de redução"
                } else if (remainingGB <= 0) {
                    "Velocidade reduzida pela operadora"
                } else {
                    "${String.format(java.util.Locale.US, "%.1f", remainingGB)} GB restantes em 5G"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (remainingGB <= 0) DangerRose else MintEmerald
            )
        }
    }
}

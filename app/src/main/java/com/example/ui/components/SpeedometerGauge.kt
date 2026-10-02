package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.WarningAmber
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedometerGauge(
    speedMbps: Float,
    maxGaugeSpeed: Float = 200f,
    size: Dp = 220.dp,
    modifier: Modifier = Modifier
) {
    val normalizedSpeed = (speedMbps / maxGaugeSpeed).coerceIn(0f, 1f)
    val animatedNormalized by animateFloatAsState(
        targetValue = normalizedSpeed,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 150f),
        label = "speedometer_needle"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = 14.dp.toPx()
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = (this.size.minDimension - strokePx * 2) / 2f

            val startAngle = 135f
            val totalSweep = 270f

            // Background Track
            drawArc(
                color = Color(0xFF1E293B),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Speed Arc Gradient
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(NeonCyan, MintEmerald, WarningAmber)
                ),
                startAngle = startAngle,
                sweepAngle = totalSweep * animatedNormalized,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Needle indicator
            val needleAngleRad = Math.toRadians((startAngle + totalSweep * animatedNormalized).toDouble())
            val needleEnd = Offset(
                x = center.x + (radius - 12.dp.toPx()) * cos(needleAngleRad).toFloat(),
                y = center.y + (radius - 12.dp.toPx()) * sin(needleAngleRad).toFloat()
            )

            drawLine(
                color = Color.White,
                start = center,
                end = needleEnd,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Center needle hub
            drawCircle(
                color = NeonCyan,
                radius = 8.dp.toPx(),
                center = center
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text(
                text = String.format(java.util.Locale.US, "%.1f", speedMbps),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Mbps",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

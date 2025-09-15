package com.rkt.snappyrulerset.presentation.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rkt.snappyrulerset.data.local.CalibrationData
import com.rkt.snappyrulerset.data.local.FrameRateMonitor
import com.rkt.snappyrulerset.domain.entity.Vec2
import com.rkt.snappyrulerset.domain.entity.degrees
import com.rkt.snappyrulerset.domain.entity.distance
import kotlin.math.absoluteValue

/**
 * Compact precision display that can be integrated into existing layouts
 */
@Composable
fun CompactPrecisionDisplay(
    calibrationData: CalibrationData,
    frameRateMonitor: FrameRateMonitor,
    currentPosition: Vec2? = null,
    lastPosition: Vec2? = null,
    currentAngle: Float? = null,
    currentRadius: Float? = null,
    hudText: String = "",
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Orientation detection for responsive design
    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    if (!isVisible) return

    val mmPerPx = calibrationData.mmPerPx
    val fps = frameRateMonitor.getCurrentFps()

    // Calculate measurements
    val distanceMm = if (currentPosition != null && lastPosition != null) {
        distance(currentPosition, lastPosition) * mmPerPx
    } else null

    val angleDeg = currentAngle?.let { degrees(it).absoluteValue }
    val radiusMm = currentRadius?.let { it * mmPerPx }

    // Determine what to show - prioritize HUD text if available
    val primaryInfo = when {
        hudText.isNotEmpty() -> hudText
        distanceMm != null -> "D: ${String.format("%.1f", distanceMm)}mm"
        angleDeg != null -> "∠: ${String.format("%.1f", angleDeg)}°"
        radiusMm != null -> "R: ${String.format("%.1f", radiusMm)}mm"
        else -> "FPS: ${String.format("%.0f", fps)}"
    }

    val secondaryInfo = when {
        hudText.isNotEmpty() -> "FPS: ${String.format("%.0f", fps)}"
        distanceMm != null -> "FPS: ${String.format("%.0f", fps)}"
        angleDeg != null -> "FPS: ${String.format("%.0f", fps)}"
        radiusMm != null -> "FPS: ${String.format("%.0f", fps)}"
        else -> "DPI: ${String.format("%.0f", calibrationData.dpi)}"
    }

    val tertiaryInfo = when {
        hudText.isNotEmpty() -> "DPI: ${String.format("%.0f", calibrationData.dpi)}"
        else -> null
    }

    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                start = if (isLandscape) 6.dp else 12.dp,
                end = if (isLandscape) 6.dp else 12.dp,
                top = 0.dp,
                bottom = if (isLandscape) 2.dp else 8.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isLandscape) 1.dp else 2.dp)
        ) {
            Text(
                text = primaryInfo,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = if (isLandscape) 10.sp else 11.sp
            )
            Text(
                text = secondaryInfo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontSize = if (isLandscape) 9.sp else 10.sp
            )
            if (tertiaryInfo != null) {
                Text(
                    text = tertiaryInfo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = if (isLandscape) 8.sp else 9.sp
                )
            }
        }
    }
}

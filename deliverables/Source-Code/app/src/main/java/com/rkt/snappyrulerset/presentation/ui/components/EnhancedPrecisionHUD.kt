package com.rkt.snappyrulerset.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rkt.snappyrulerset.data.local.CalibrationData
import com.rkt.snappyrulerset.data.local.FrameRateMonitor
import com.rkt.snappyrulerset.domain.entity.Vec2
import com.rkt.snappyrulerset.domain.entity.degrees
import com.rkt.snappyrulerset.domain.entity.distance
import kotlin.math.*

/**
 * Enhanced Precision HUD with real-time measurements and performance monitoring
 */
@Composable
fun EnhancedPrecisionHUD(
    calibrationData: CalibrationData,
    frameRateMonitor: FrameRateMonitor,
    currentPosition: Vec2? = null,
    lastPosition: Vec2? = null,
    currentAngle: Float? = null,
    currentRadius: Float? = null,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 200.dp, max = 280.dp)
                .padding(top = 56.dp, end = 16.dp, bottom = 16.dp, start = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header with expand/collapse
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Precision",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // FPS Counter
                        FPSIndicator(frameRateMonitor = frameRateMonitor)
                        
                        // Expand/Collapse button
                        IconButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                // Measurements Section (only when expanded)
                if (isExpanded && (currentPosition != null || currentAngle != null || currentRadius != null)) {
                    MeasurementsSection(
                        currentPosition = currentPosition,
                        lastPosition = lastPosition,
                        currentAngle = currentAngle,
                        currentRadius = currentRadius,
                        calibrationData = calibrationData
                    )
                }
                
                // Calibration Info (only when expanded)
                if (isExpanded) {
                    CalibrationInfoSection(calibrationData = calibrationData)
                }
            }
        }
    }
}

@Composable
private fun FPSIndicator(
    frameRateMonitor: FrameRateMonitor,
    modifier: Modifier = Modifier
) {
    var fps by remember { mutableStateOf(frameRateMonitor.getCurrentFps()) }
    
    // Update FPS periodically
    LaunchedEffect(frameRateMonitor) {
        while (true) {
            fps = frameRateMonitor.getCurrentFps()
            kotlinx.coroutines.delay(1000) // Update every second
        }
    }
    val fpsColor by animateFloatAsState(
        targetValue = when {
            fps >= 55f -> 0f // Green
            fps >= 45f -> 0.5f // Yellow
            else -> 1f // Red
        },
        animationSpec = tween(300),
        label = "fpsColor"
    )
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Speed,
            contentDescription = "FPS",
            tint = when {
                fpsColor <= 0.2f -> Color(0xFF4CAF50) // Green
                fpsColor <= 0.6f -> Color(0xFFFFC107) // Yellow
                else -> Color(0xFFFF5722) // Red
            },
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "${fps.toInt()} FPS",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = when {
                fpsColor <= 0.2f -> Color(0xFF4CAF50) // Green
                fpsColor <= 0.6f -> Color(0xFFFFC107) // Yellow
                else -> Color(0xFFFF5722) // Red
            }
        )
    }
}

@Composable
private fun MeasurementsSection(
    currentPosition: Vec2?,
    lastPosition: Vec2?,
    currentAngle: Float?,
    currentRadius: Float?,
    calibrationData: CalibrationData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Measurements",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Position measurements
        if (currentPosition != null) {
            PositionMeasurement(
                position = currentPosition,
                calibrationData = calibrationData
            )
        }
        
        // Distance measurement
        if (currentPosition != null && lastPosition != null) {
            DistanceMeasurement(
                from = lastPosition,
                to = currentPosition,
                calibrationData = calibrationData
            )
        }
        
        // Angle measurement
        if (currentAngle != null) {
            AngleMeasurement(angle = currentAngle)
        }
        
        // Radius measurement
        if (currentRadius != null) {
            RadiusMeasurement(
                radius = currentRadius,
                calibrationData = calibrationData
            )
        }
    }
}

@Composable
private fun PositionMeasurement(
    position: Vec2,
    calibrationData: CalibrationData,
    modifier: Modifier = Modifier
) {
    val mmX = position.x * calibrationData.mmPerPx
    val mmY = position.y * calibrationData.mmPerPx
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Position:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = "X: ${String.format("%.1f", mmX)}mm, Y: ${String.format("%.1f", mmY)}mm",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DistanceMeasurement(
    from: Vec2,
    to: Vec2,
    calibrationData: CalibrationData,
    modifier: Modifier = Modifier
) {
    val distancePx = distance(from, to)
    val distanceMm = distancePx * calibrationData.mmPerPx
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Distance:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = "${String.format("%.1f", distanceMm)}mm",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun AngleMeasurement(
    angle: Float,
    modifier: Modifier = Modifier
) {
    val angleDegrees = degrees(angle)
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Angle:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = "${String.format("%.1f", angleDegrees)}°",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun RadiusMeasurement(
    radius: Float,
    calibrationData: CalibrationData,
    modifier: Modifier = Modifier
) {
    val radiusMm = radius * calibrationData.mmPerPx
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Radius:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = "${String.format("%.1f", radiusMm)}mm",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun CalibrationInfoSection(
    calibrationData: CalibrationData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Calibration",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Calibration Info",
                tint = if (calibrationData.isCalibrated) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "DPI:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "${String.format("%.0f", calibrationData.dpi)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "mm/px:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "${String.format("%.3f", calibrationData.mmPerPx)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Status:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = if (calibrationData.isCalibrated) "Calibrated" else "Default",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (calibrationData.isCalibrated) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.secondary
            )
        }
    }
}

package com.rkt.snappyrulerset.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.rkt.snappyrulerset.domain.entity.Vec2
import com.rkt.snappyrulerset.domain.entity.degrees
import com.rkt.snappyrulerset.domain.entity.fromAngle
import com.rkt.snappyrulerset.domain.entity.radians
import com.rkt.snappyrulerset.domain.usecase.SnapCandidate
import kotlin.math.*

/**
 * Advanced Visual System for enhanced user experience
 */
object AdvancedVisualSystem {
    
    /**
     * Color scheme for different snap types
     */
    object Colors {
        val PointSnap = Color(0xFF00FF00) // Green
        val SegmentSnap = Color(0xFF0088FF) // Blue
        val GridSnap = Color(0xFFFF8800) // Orange
        val AngleSnap = Color(0xFFFF0088) // Pink
        val CompetingSnap = Color(0xFF888888) // Gray
    }
    
    /**
     * Animation specifications
     */
    object Animations {
        val SnapPulse = infiniteRepeatable(
            animation = tween<Float>(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
        val SnapTick = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)
        val CompetingSnap = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)
    }
}

/**
 * Enhanced snap point indicator with animations
 */
@Composable
fun EnhancedSnapPointIndicator(
    position: Vec2,
    snapType: SnapType,
    isActive: Boolean = false,
    isPulsing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val color = when (snapType) {
        SnapType.POINT -> AdvancedVisualSystem.Colors.PointSnap
        SnapType.SEGMENT -> AdvancedVisualSystem.Colors.SegmentSnap
        SnapType.GRID -> AdvancedVisualSystem.Colors.GridSnap
        SnapType.ANGLE -> AdvancedVisualSystem.Colors.AngleSnap
    }
    
    val size = if (isActive) 16.dp else 12.dp
    
    // Pulsing animation
    val pulseScale by animateFloatAsState(
        targetValue = if (isPulsing) 1.3f else 1f,
        animationSpec = AdvancedVisualSystem.Animations.SnapPulse,
        label = "pulseScale"
    )
    
    Canvas(
        modifier = modifier.size(size)
    ) {
        val center = Offset(size.toPx() / 2, size.toPx() / 2)
        val radius = (size.toPx() / 2) * pulseScale
        
        // Draw outer glow
        drawCircle(
            color = color.copy(alpha = 0.2f),
            radius = radius * 2f,
            center = center
        )
        
        // Draw main circle
        drawCircle(
            color = color,
            radius = radius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
        
        // Draw center dot
        drawCircle(
            color = color,
            radius = radius * 0.3f,
            center = center
        )
        
        // Draw type-specific indicators
        when (snapType) {
            SnapType.POINT -> {
                // Draw crosshair
                val crossSize = radius * 0.8f
                drawLine(
                    color = color,
                    start = Offset(center.x - crossSize, center.y),
                    end = Offset(center.x + crossSize, center.y),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = color,
                    start = Offset(center.x, center.y - crossSize),
                    end = Offset(center.x, center.y + crossSize),
                    strokeWidth = 1.dp.toPx()
                )
            }
            SnapType.SEGMENT -> {
                // Draw line indicator
                val lineLength = radius * 1.2f
                drawLine(
                    color = color,
                    start = Offset(center.x - lineLength, center.y),
                    end = Offset(center.x + lineLength, center.y),
                    strokeWidth = 2.dp.toPx()
                )
            }
            SnapType.GRID -> {
                // Draw grid pattern
                val gridSize = radius * 0.6f
                drawLine(
                    color = color,
                    start = Offset(center.x - gridSize, center.y - gridSize),
                    end = Offset(center.x + gridSize, center.y + gridSize),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = color,
                    start = Offset(center.x + gridSize, center.y - gridSize),
                    end = Offset(center.x - gridSize, center.y + gridSize),
                    strokeWidth = 1.dp.toPx()
                )
            }
            SnapType.ANGLE -> {
                // Draw angle arc
                drawArc(
                    color = color,
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * 0.8f, center.y - radius * 0.8f),
                    size = androidx.compose.ui.geometry.Size(radius * 1.6f, radius * 1.6f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

/**
 * Competing snap indicators with priority visualization
 */
@Composable
fun CompetingSnapIndicators(
    snaps: List<SnapCandidate>,
    selectedSnap: SnapCandidate?,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible && snaps.isNotEmpty(),
        enter = fadeIn(animationSpec = AdvancedVisualSystem.Animations.CompetingSnap),
        exit = fadeOut(animationSpec = AdvancedVisualSystem.Animations.CompetingSnap)
    ) {
        Canvas(modifier = modifier) {
            snaps.forEach { snap ->
                val isSelected = snap == selectedSnap
                val alpha = if (isSelected) 1f else 0.6f
                val size = if (isSelected) 16.dp.toPx() else 12.dp.toPx()
                
                val color = when (snap.priority) {
                    1 -> AdvancedVisualSystem.Colors.PointSnap
                    2 -> AdvancedVisualSystem.Colors.SegmentSnap
                    3 -> AdvancedVisualSystem.Colors.GridSnap
                    else -> AdvancedVisualSystem.Colors.CompetingSnap
                }.copy(alpha = alpha)
                
                val center = Offset(snap.pos.x, snap.pos.y)
                
                // Draw outer ring
                drawCircle(
                    color = color.copy(alpha = 0.3f),
                    radius = size * 1.5f,
                    center = center
                )
                
                // Draw main circle
                drawCircle(
                    color = color,
                    radius = size / 2,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
                
                // Draw priority indicator
                if (isSelected) {
                    drawCircle(
                        color = color,
                        radius = size / 4,
                        center = center
                    )
                }
                
                // Draw priority number
                if (snaps.size > 1) {
                    val priorityText = snap.priority.toString()
                    // Note: Text drawing would require additional setup with drawContext
                }
            }
        }
    }
}

/**
 * Enhanced snap tick animation with visual effects
 */
@Composable
fun EnhancedSnapTickAnimation(
    position: Vec2,
    isVisible: Boolean = true,
    snapType: SnapType = SnapType.POINT,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = AdvancedVisualSystem.Animations.SnapTick),
        exit = fadeOut(animationSpec = AdvancedVisualSystem.Animations.SnapTick)
    ) {
        Canvas(modifier = modifier) {
            val center = Offset(position.x, position.y)
            val tickSize = 24.dp.toPx()
            val color = when (snapType) {
                SnapType.POINT -> AdvancedVisualSystem.Colors.PointSnap
                SnapType.SEGMENT -> AdvancedVisualSystem.Colors.SegmentSnap
                SnapType.GRID -> AdvancedVisualSystem.Colors.GridSnap
                SnapType.ANGLE -> AdvancedVisualSystem.Colors.AngleSnap
            }
            
            // Draw animated tick
            drawLine(
                color = color,
                start = Offset(center.x - tickSize/2, center.y),
                end = Offset(center.x + tickSize/2, center.y),
                strokeWidth = 4.dp.toPx()
            )
            
            // Draw checkmark
            drawLine(
                color = color,
                start = Offset(center.x - tickSize/4, center.y),
                end = Offset(center.x, center.y + tickSize/4),
                strokeWidth = 4.dp.toPx()
            )
            drawLine(
                color = color,
                start = Offset(center.x, center.y + tickSize/4),
                end = Offset(center.x + tickSize/2, center.y - tickSize/4),
                strokeWidth = 4.dp.toPx()
            )
            
            // Draw ripple effect
            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = tickSize * 1.5f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

/**
 * Enhanced angle snap indicator with arc visualization
 */
@Composable
fun EnhancedAngleSnapIndicator(
    center: Vec2,
    angle: Float,
    snapType: AngleSnapType,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val color = when (snapType) {
        AngleSnapType.HARD -> AdvancedVisualSystem.Colors.AngleSnap
        AngleSnapType.SOFT -> AdvancedVisualSystem.Colors.SegmentSnap
    }
    
    val alpha = if (isActive) 1f else 0.7f
    
    Canvas(modifier = modifier) {
        val centerOffset = Offset(center.x, center.y)
        val radius = 40.dp.toPx()
        
        // Draw angle arc
        drawArc(
            color = color.copy(alpha = alpha * 0.3f),
            startAngle = angle - 10f,
            sweepAngle = 20f,
            useCenter = false,
            topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = 4.dp.toPx())
        )
        
        // Draw angle line
        val direction = fromAngle(angle)
        val endPoint = centerOffset + Offset(direction.x * radius, direction.y * radius)
        
        drawLine(
            color = color.copy(alpha = alpha),
            start = centerOffset,
            end = endPoint,
            strokeWidth = 3.dp.toPx()
        )
        
        // Draw angle text background
        val textRadius = radius * 0.7f
        val textCenter = centerOffset + Offset(
            cos(angle) * textRadius,
            -sin(angle) * textRadius
        )
        
        drawCircle(
            color = color.copy(alpha = alpha * 0.8f),
            radius = 12.dp.toPx(),
            center = textCenter
        )
    }
}

/**
 * Enhanced grid visualization with animated lines
 */
@Composable
fun EnhancedGridVisualization(
    gridSpacingMm: Float,
    viewport: com.rkt.snappyrulerset.domain.entity.Viewport,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Canvas(modifier = modifier) {
            val gridColor = Color.Gray.copy(alpha = 0.3f)
            val majorGridColor = Color.Gray.copy(alpha = 0.5f)
            
            val gridSpacingPx = gridSpacingMm * 25.4f / 160f // Convert mm to px (approximate)
            val zoomedSpacing = gridSpacingPx * viewport.zoom
            
            if (zoomedSpacing > 4f) { // Only draw grid if spacing is visible
                val startX = (viewport.pan.x % zoomedSpacing).let { if (it < 0) it + zoomedSpacing else it }
                val startY = (viewport.pan.y % zoomedSpacing).let { if (it < 0) it + zoomedSpacing else it }
                
                // Draw vertical lines
                var x = startX
                while (x < size.width) {
                    val isMajor = (x / zoomedSpacing).toInt() % 5 == 0
                    drawLine(
                        color = if (isMajor) majorGridColor else gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = if (isMajor) 1.dp.toPx() else 0.5.dp.toPx()
                    )
                    x += zoomedSpacing
                }
                
                // Draw horizontal lines
                var y = startY
                while (y < size.height) {
                    val isMajor = (y / zoomedSpacing).toInt() % 5 == 0
                    drawLine(
                        color = if (isMajor) majorGridColor else gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = if (isMajor) 1.dp.toPx() else 0.5.dp.toPx()
                    )
                    y += zoomedSpacing
                }
            }
        }
    }
}

/**
 * Enhanced measurement display with animations
 */
@Composable
fun EnhancedMeasurementDisplay(
    measurements: Map<String, String>,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible && measurements.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                measurements.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Enhanced tool visual feedback with animations
 */
@Composable
fun EnhancedToolVisualFeedback(
    toolType: ToolType,
    isActive: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val color = when {
        isActive -> MaterialTheme.colorScheme.primary
        isSelected -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.2f else if (isSelected) 1.1f else 1f,
        animationSpec = tween(200),
        label = "toolScale"
    )
    
    Canvas(
        modifier = modifier.size(24.dp)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = (size.minDimension / 2) * scale
        
        // Draw selection ring
        if (isSelected || isActive) {
            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = radius * 1.5f,
                center = center
            )
        }
        
        // Draw main circle
        drawCircle(
            color = color,
            radius = radius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
        
        // Draw tool-specific indicator
        when (toolType) {
            ToolType.RULER -> {
                // Draw ruler lines
                drawLine(
                    color = color,
                    start = Offset(center.x - radius * 0.8f, center.y),
                    end = Offset(center.x + radius * 0.8f, center.y),
                    strokeWidth = 2.dp.toPx()
                )
            }
            ToolType.SET_SQUARE -> {
                // Draw triangle
                val triangleSize = radius * 0.8f
                val path = Path().apply {
                    moveTo(center.x, center.y - triangleSize)
                    lineTo(center.x + triangleSize, center.y + triangleSize)
                    lineTo(center.x - triangleSize, center.y + triangleSize)
                    close()
                }
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            ToolType.PROTRACTOR -> {
                // Draw semicircle
                drawArc(
                    color = color,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * 0.8f, center.y - radius * 0.8f),
                    size = androidx.compose.ui.geometry.Size(radius * 1.6f, radius * 1.6f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            ToolType.COMPASS -> {
                // Draw circle
                drawCircle(
                    color = color,
                    radius = radius * 0.8f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}


/**
 * Tool types for visual feedback
 */
enum class ToolType {
    RULER,
    SET_SQUARE,
    PROTRACTOR,
    COMPASS
}

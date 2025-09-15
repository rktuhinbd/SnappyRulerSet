package com.rkt.snappyrulerset.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.rkt.snappyrulerset.domain.entity.Vec2
import com.rkt.snappyrulerset.domain.entity.degrees
import com.rkt.snappyrulerset.domain.entity.fromAngle
import com.rkt.snappyrulerset.domain.entity.radians
import com.rkt.snappyrulerset.domain.usecase.SnapCandidate
import kotlin.math.*

/**
 * Enhanced visual feedback components for snapping system
 */

/**
 * Visual feedback for snap points with different types and priorities
 */
@Composable
fun SnapPointIndicator(
    position: Vec2,
    snapType: SnapType,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val color = when (snapType) {
        SnapType.POINT -> if (isActive) Color(0xFF00FF00) else Color(0xFF00AA00)
        SnapType.SEGMENT -> if (isActive) Color(0xFF0088FF) else Color(0xFF0066CC)
        SnapType.GRID -> if (isActive) Color(0xFFFF8800) else Color(0xFFCC6600)
        SnapType.ANGLE -> if (isActive) Color(0xFFFF0088) else Color(0xFFCC0066)
    }
    
    val size = if (isActive) 12.dp else 8.dp
    
    Canvas(
        modifier = modifier.size(size)
    ) {
        val center = Offset(size.toPx() / 2, size.toPx() / 2)
        val radius = size.toPx() / 2
        
        // Draw outer ring
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = radius * 1.5f,
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
                // Draw crosshairs for point snaps
                val crosshairSize = radius * 0.8f
                drawLine(
                    color = color,
                    start = Offset(center.x - crosshairSize, center.y),
                    end = Offset(center.x + crosshairSize, center.y),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = color,
                    start = Offset(center.x, center.y - crosshairSize),
                    end = Offset(center.x, center.y + crosshairSize),
                    strokeWidth = 1.dp.toPx()
                )
            }
            SnapType.SEGMENT -> {
                // Draw line indicator for segment snaps
                val lineLength = radius * 1.2f
                drawLine(
                    color = color,
                    start = Offset(center.x - lineLength, center.y),
                    end = Offset(center.x + lineLength, center.y),
                    strokeWidth = 2.dp.toPx()
                )
            }
            SnapType.GRID -> {
                // Draw grid pattern for grid snaps
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
                // Draw angle indicator for angle snaps
                val angleSize = radius * 0.8f
                drawArc(
                    color = color,
                    startAngle = -45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(center.x - angleSize, center.y - angleSize),
                    size = androidx.compose.ui.geometry.Size(angleSize * 2, angleSize * 2),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

/**
 * Visual feedback for snap radius
 */
@Composable
fun SnapRadiusIndicator(
    center: Vec2,
    radius: Float,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    
    Canvas(modifier = modifier) {
        val centerOffset = Offset(center.x, center.y)
        
        // Draw snap radius circle
        drawCircle(
            color = Color(0xFF00FF00).copy(alpha = 0.1f),
            radius = radius,
            center = centerOffset,
            style = Stroke(width = 1.dp.toPx())
        )
        
        // Draw radius lines at 0°, 45°, 90°, 135°, 180°, 225°, 270°, 315°
        for (angle in 0..7) {
            val angleRad = radians(angle * 45f)
            val direction = fromAngle(angleRad)
            val endPoint = centerOffset + Offset(direction.x * radius, direction.y * radius)
            
            drawLine(
                color = Color(0xFF00FF00).copy(alpha = 0.3f),
                start = centerOffset,
                end = endPoint,
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

/**
 * Visual feedback for angle snapping
 */
@Composable
fun AngleSnapIndicator(
    center: Vec2,
    angle: Float,
    snapType: AngleSnapType,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val color = when (snapType) {
        AngleSnapType.HARD -> if (isActive) Color(0xFFFF0088) else Color(0xFFCC0066)
        AngleSnapType.SOFT -> if (isActive) Color(0xFF0088FF) else Color(0xFF0066CC)
    }
    
    Canvas(modifier = modifier) {
        val centerOffset = Offset(center.x, center.y)
        val radius = 30.dp.toPx()
        
        // Draw angle arc
        drawArc(
            color = color.copy(alpha = 0.3f),
            startAngle = angle - 5f,
            sweepAngle = 10f,
            useCenter = false,
            topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Draw angle line
        val direction = fromAngle(angle)
        val endPoint = centerOffset + Offset(direction.x * radius, direction.y * radius)
        
        drawLine(
            color = color,
            start = centerOffset,
            end = endPoint,
            strokeWidth = 2.dp.toPx()
        )
        
        // Draw angle text
        val angleDegrees = degrees(angle)
        val text = "${angleDegrees.toInt()}°"
        // Note: Text drawing would require additional setup with drawContext
    }
}

/**
 * Visual feedback for competing snaps
 */
@Composable
fun CompetingSnapIndicator(
    snaps: List<SnapCandidate>,
    selectedSnap: SnapCandidate?,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        snaps.forEach { snap ->
            val isSelected = snap == selectedSnap
            val alpha = if (isSelected) 1f else 0.5f
            val size = if (isSelected) 12.dp.toPx() else 8.dp.toPx()
            
            val color = when (snap.priority) {
                1 -> Color(0xFF00FF00) // Point snaps
                2 -> Color(0xFF0088FF) // Segment snaps
                3 -> Color(0xFFFF8800) // Grid snaps
                else -> Color(0xFF888888)
            }.copy(alpha = alpha)
            
            val center = Offset(snap.pos.x, snap.pos.y)
            
            // Draw snap indicator
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
        }
    }
}

/**
 * Enhanced snap tick animation
 */
@Composable
fun SnapTickAnimation(
    position: Vec2,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    
    Canvas(modifier = modifier) {
        val center = Offset(position.x, position.y)
        val tickSize = 20.dp.toPx()
        
        // Draw animated tick
        drawLine(
            color = Color(0xFF00FF00),
            start = Offset(center.x - tickSize/2, center.y),
            end = Offset(center.x + tickSize/2, center.y),
            strokeWidth = 3.dp.toPx()
        )
        
        // Draw checkmark
        drawLine(
            color = Color(0xFF00FF00),
            start = Offset(center.x - tickSize/4, center.y),
            end = Offset(center.x, center.y + tickSize/4),
            strokeWidth = 3.dp.toPx()
        )
        drawLine(
            color = Color(0xFF00FF00),
            start = Offset(center.x, center.y + tickSize/4),
            end = Offset(center.x + tickSize/2, center.y - tickSize/4),
            strokeWidth = 3.dp.toPx()
        )
    }
}

/**
 * Types of snap points
 */
enum class SnapType {
    POINT,      // Endpoints, midpoints, circle centers
    SEGMENT,    // Closest point on line segments
    GRID,       // Grid intersections
    ANGLE       // Angle snapping
}

/**
 * Types of angle snapping
 */
enum class AngleSnapType {
    HARD,       // Strong snap to common angles
    SOFT        // Gentle snap to nearby angles
}


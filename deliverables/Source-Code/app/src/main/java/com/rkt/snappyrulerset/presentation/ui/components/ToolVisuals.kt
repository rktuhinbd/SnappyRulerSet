package com.rkt.snappyrulerset.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
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
import kotlin.math.*

/**
 * Enhanced visual representations for drawing tools
 */

/**
 * Ruler visual with measurement markings
 */
@Composable
fun RulerVisual(
    position: Vec2,
    rotation: Float,
    length: Float = 200f,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val color = if (isActive) Color(0xFF2196F3) else Color(0xFF1976D2)
    val strokeWidth = if (isActive) 3.dp else 2.dp

    Canvas(
        modifier = modifier.size((length + 40).dp, 40.dp)
    ) {
        val center = Offset(this.size.width / 2, this.size.height / 2)
        val direction = fromAngle(rotation)
        val startPoint = center + Offset(-direction.x * length / 2, -direction.y * length / 2)
        val endPoint = center + Offset(direction.x * length / 2, direction.y * length / 2)

        // Draw main ruler body
        drawLine(
            color = color,
            start = startPoint,
            end = endPoint,
            strokeWidth = strokeWidth.toPx()
        )

        // Draw measurement markings
        val markingInterval = 20f // 20px intervals
        val numMarkings = (length / markingInterval).toInt()

        for (i in 0..numMarkings) {
            val t = i.toFloat() / numMarkings
            val markingPos = Offset(
                startPoint.x + (endPoint.x - startPoint.x) * t,
                startPoint.y + (endPoint.y - startPoint.y) * t
            )

            val markingLength = if (i % 5 == 0) 15f else 8f // Major markings every 5th
            val perpendicular = Offset(-direction.y, direction.x)

            drawLine(
                color = color,
                start = markingPos + Offset(
                    perpendicular.x * markingLength / 2,
                    perpendicular.y * markingLength / 2
                ),
                end = markingPos + Offset(
                    -perpendicular.x * markingLength / 2,
                    -perpendicular.y * markingLength / 2
                ),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw center point
        drawCircle(
            color = color,
            radius = 4.dp.toPx(),
            center = center
        )
    }
}

/**
 * Set square (triangle) visual with angle markings
 */
@Composable
fun SetSquareVisual(
    position: Vec2,
    rotation: Float,
    size: Float = 120f,
    is45Degree: Boolean = true,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val color = if (isActive) Color(0xFF4CAF50) else Color(0xFF388E3C)
    val strokeWidth = if (isActive) 3.dp else 2.dp

    Canvas(
        modifier = modifier.size((size + 20).dp, (size + 20).dp)
    ) {
        val center = Offset(this.size.width / 2, this.size.height / 2)

        // Calculate triangle points
        val triangleSize = size / 2
        val points = if (is45Degree) {
            // 45-45-90 triangle
            listOf(
                center + Offset(0f, -triangleSize),
                center + Offset(triangleSize, triangleSize),
                center + Offset(-triangleSize, triangleSize)
            )
        } else {
            // 30-60-90 triangle
            val height = triangleSize * sqrt(3f) / 2
            listOf(
                center + Offset(0f, -height),
                center + Offset(triangleSize, height),
                center + Offset(-triangleSize, height)
            )
        }

        // Rotate points around center
        val rotatedPoints = points.map { point ->
            val relativeX = point.x - center.x
            val relativeY = point.y - center.y
            val cos = cos(rotation)
            val sin = sin(rotation)
            Offset(
                center.x + relativeX * cos - relativeY * sin,
                center.y + relativeX * sin + relativeY * cos
            )
        }

        // Draw triangle outline
        for (i in rotatedPoints.indices) {
            val start = rotatedPoints[i]
            val end = rotatedPoints[(i + 1) % rotatedPoints.size]
            drawLine(
                color = color,
                start = start,
                end = end,
                strokeWidth = strokeWidth.toPx()
            )
        }

        // Draw angle markings
        if (is45Degree) {
            // 45-degree angle arc
            drawArc(
                color = color.copy(alpha = 0.6f),
                startAngle = degrees(rotation - 45f),
                sweepAngle = 45f,
                useCenter = false,
                topLeft = Offset(center.x - 20, center.y - 20),
                size = androidx.compose.ui.geometry.Size(40f, 40f),
                style = Stroke(width = 1.dp.toPx())
            )
        } else {
            // 30-degree and 60-degree angle arcs
            drawArc(
                color = color.copy(alpha = 0.6f),
                startAngle = degrees(rotation - 30f),
                sweepAngle = 30f,
                useCenter = false,
                topLeft = Offset(center.x - 15, center.y - 15),
                size = androidx.compose.ui.geometry.Size(30f, 30f),
                style = Stroke(width = 1.dp.toPx())
            )

            drawArc(
                color = color.copy(alpha = 0.6f),
                startAngle = degrees(rotation - 60f),
                sweepAngle = 60f,
                useCenter = false,
                topLeft = Offset(center.x - 25, center.y - 25),
                size = androidx.compose.ui.geometry.Size(50f, 50f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Draw center point
        drawCircle(
            color = color,
            radius = 3.dp.toPx(),
            center = center
        )
    }
}

/**
 * Protractor visual with degree markings
 */
@Composable
fun ProtractorVisual(
    position: Vec2,
    rotation: Float,
    radius: Float = 80f,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val color = if (isActive) Color(0xFFFF9800) else Color(0xFFF57C00)
    val strokeWidth = if (isActive) 3.dp else 2.dp

    Canvas(
        modifier = modifier.size((radius * 2 + 20).dp, (radius * 2 + 20).dp)
    ) {
        val center = Offset(this.size.width / 2, this.size.height / 2)

        // Draw semicircle
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth.toPx())
        )

        // Draw degree markings
        for (angle in 0..180 step 10) {
            val angleRad = radians(angle.toFloat())
            val innerRadius = radius - 10
            val outerRadius = radius + 5

            val startPoint = center + Offset(
                cos(angleRad) * innerRadius,
                -sin(angleRad) * innerRadius
            )
            val endPoint = center + Offset(
                cos(angleRad) * outerRadius,
                -sin(angleRad) * outerRadius
            )

            val markingLength = if (angle % 30 == 0) 15f else 8f

            drawLine(
                color = color,
                start = startPoint,
                end = endPoint,
                strokeWidth = if (angle % 30 == 0) 2.dp.toPx() else 1.dp.toPx()
            )
        }

        // Draw center point
        drawCircle(
            color = color,
            radius = 4.dp.toPx(),
            center = center
        )

        // Draw rotation indicator
        val indicatorAngle = radians(rotation)
        val indicatorEnd = center + Offset(
            cos(indicatorAngle) * (radius - 20),
            -sin(indicatorAngle) * (radius - 20)
        )

        drawLine(
            color = color,
            start = center,
            end = indicatorEnd,
            strokeWidth = 3.dp.toPx()
        )

        // Draw angle text (simplified as a line for now)
        if (isActive) {
            val textAngle = degrees(rotation)
            val textRadius = radius - 30
            val textPos = center + Offset(
                cos(indicatorAngle) * textRadius,
                -sin(indicatorAngle) * textRadius
            )

            // Draw angle value indicator
            drawCircle(
                color = color,
                radius = 8.dp.toPx(),
                center = textPos
            )
        }
    }
}

/**
 * Compass visual with radius markings
 */
@Composable
fun CompassVisual(
    position: Vec2,
    radius: Float = 60f,
    isActive: Boolean = false,
    isArcMode: Boolean = false,
    startAngle: Float? = null,
    endAngle: Float? = null,
    modifier: Modifier = Modifier
) {
    val color = if (isActive) Color(0xFF9C27B0) else Color(0xFF7B1FA2)
    val strokeWidth = if (isActive) 3.dp else 2.dp

    Canvas(
        modifier = modifier.size((radius * 2 + 20).dp, (radius * 2 + 20).dp)
    ) {
        val center = Offset(this.size.width / 2, this.size.height / 2)

        if (isArcMode && startAngle != null && endAngle != null) {
            // Draw arc
            val sweepAngle = endAngle - startAngle
            drawArc(
                color = color,
                startAngle = degrees(startAngle),
                sweepAngle = degrees(sweepAngle),
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth.toPx())
            )

            // Draw arc endpoints
            val startPoint = center + Offset(
                cos(startAngle) * radius,
                -sin(startAngle) * radius
            )
            val endPoint = center + Offset(
                cos(endAngle) * radius,
                -sin(endAngle) * radius
            )

            drawCircle(
                color = color,
                radius = 6.dp.toPx(),
                center = startPoint
            )
            drawCircle(
                color = color,
                radius = 6.dp.toPx(),
                center = endPoint
            )
        } else {
            // Draw full circle
            drawCircle(
                color = color,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth.toPx())
            )
        }

        // Draw radius markings
        for (i in 1..4) {
            val markingRadius = radius * i / 4
            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = markingRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Draw center point
        drawCircle(
            color = color,
            radius = 4.dp.toPx(),
            center = center
        )

        // Draw compass needle (pointing up)
        val needleLength = radius * 0.8f
        val needleEnd = center + Offset(0f, -needleLength)

        drawLine(
            color = color,
            start = center,
            end = needleEnd,
            strokeWidth = 2.dp.toPx()
        )

        // Draw needle arrow
        val arrowSize = 8f
        val arrowLeft = needleEnd + Offset(-arrowSize, arrowSize)
        val arrowRight = needleEnd + Offset(arrowSize, arrowSize)

        drawLine(
            color = color,
            start = needleEnd,
            end = arrowLeft,
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = color,
            start = needleEnd,
            end = arrowRight,
            strokeWidth = 2.dp.toPx()
        )
    }
}

/**
 * Tool selection indicator
 */
@Composable
fun ToolSelectionIndicator(
    position: Vec2,
    toolType: ToolType,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val color = if (isSelected) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
    val size = if (isSelected) 16.dp else 12.dp

    Canvas(
        modifier = modifier.size(size)
    ) {
        val center = Offset(size.toPx() / 2, size.toPx() / 2)
        val radius = size.toPx() / 2

        // Draw selection ring
        drawCircle(
            color = color,
            radius = radius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw tool-specific indicator
        when (toolType) {
            ToolType.RULER -> {
                // Draw ruler icon
                drawLine(
                    color = color,
                    start = Offset(center.x - radius * 0.6f, center.y),
                    end = Offset(center.x + radius * 0.6f, center.y),
                    strokeWidth = 2.dp.toPx()
                )
            }

            ToolType.SET_SQUARE -> {
                // Draw triangle icon
                val triangleSize = radius * 0.6f
                val points = listOf(
                    Offset(center.x, center.y - triangleSize),
                    Offset(center.x + triangleSize, center.y + triangleSize),
                    Offset(center.x - triangleSize, center.y + triangleSize)
                )
                for (i in points.indices) {
                    val start = points[i]
                    val end = points[(i + 1) % points.size]
                    drawLine(
                        color = color,
                        start = start,
                        end = end,
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            ToolType.PROTRACTOR -> {
                // Draw semicircle icon
                drawArc(
                    color = color,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * 0.6f, center.y - radius * 0.6f),
                    size = androidx.compose.ui.geometry.Size(radius * 1.2f, radius * 1.2f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            ToolType.COMPASS -> {
                // Draw circle icon
                drawCircle(
                    color = color,
                    radius = radius * 0.6f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

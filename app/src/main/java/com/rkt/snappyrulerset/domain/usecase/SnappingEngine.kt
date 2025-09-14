package com.rkt.snappyrulerset.domain.usecase

import androidx.compose.runtime.Immutable
import com.rkt.snappyrulerset.domain.entity.Vec2
import com.rkt.snappyrulerset.domain.entity.distance
import com.rkt.snappyrulerset.domain.entity.minus
import com.rkt.snappyrulerset.domain.entity.radians
import kotlin.math.*


/**
 * Represents a snap target with position, priority, distance, and label information.
 * Used by the snapping system to determine the best snap point for user input.
 */
@Immutable
data class SnapCandidate(
    val pos: Vec2,
    val priority: Int,
    val distancePx: Float,
    val label: String
)

/**
 * Engine responsible for calculating snap targets and angle snapping.
 * Provides functionality for grid snapping, angle snapping, and dynamic snap radius calculation.
 */
class SnappingEngine {
    // Common angles in radians
    private val angleSet = floatArrayOf(0f, 30f, 45f, 60f, 90f, 120f, 135f, 150f, 180f)
        .map { radians(it) }.toFloatArray()


    /**
     * Converts millimeters to pixels based on device DPI
     */
    fun mmToPx(mm: Float, dpi: Float): Float = (dpi / 25.4f) * mm

    /**
     * Calculates a dynamic snap radius based on the current zoom level.
     * The snap radius increases at lower zoom levels and decreases at higher zoom levels
     * to maintain consistent user experience.
     */
    fun dynamicSnapRadiusPx(zoom: Float, basePx: Float = 16f): Float =
        (basePx / zoom).coerceIn(6f, 28f)

    /**
     * Snaps a point to the nearest grid intersection
     */
    fun snapToGrid(current: Vec2, gridMm: Float, dpi: Float, zoom: Float): SnapCandidate {
        val gridPx = mmToPx(gridMm, dpi) * zoom // grid spacing in screen px for clarity
        // We compute in world coords, so use world grid; spacing in world px = mmToPx(...)
        val spacing = mmToPx(gridMm, dpi)
        fun roundTo(v: Float, s: Float) = (round(v / s) * s)
        val gx = roundTo(current.x, spacing)
        val gy = roundTo(current.y, spacing)
        val snapped = Vec2(gx, gy)
        val d = distance(current, snapped)
        return SnapCandidate(snapped, priority = 10, distancePx = d * zoom, label = "grid")
    }

    /**
     * Snaps an angle to the nearest common angle (0°, 30°, 45°, 60°, 90°, etc.)
     */
    fun snapAngle(from: Vec2, to: Vec2): Pair<Float, Float> {
        val v = to - from
        val a = atan2(v.y, v.x)
        val best = angleSet.minBy { abs(angleDelta(a, it)) }
        return a to best
    }


    private fun angleDelta(a: Float, b: Float): Float {
        var d = abs(a - b) % (2f * Math.PI.toFloat())
        if (d > Math.PI) d = 2f * Math.PI.toFloat() - d
        return d
    }
}
package com.rkt.snappyrulerset.domain.usecase

import com.rkt.snappyrulerset.domain.entity.Vec2
import com.rkt.snappyrulerset.domain.entity.radians
import org.junit.Test
import org.junit.Assert.*
import kotlin.math.*

class SnappingEngineTest {

    private val snappingEngine = SnappingEngine()

    @Test
    fun `mmToPx should convert correctly`() {
        val result = snappingEngine.mmToPx(25.4f, 160f)
        assertEquals(160f, result, 0.001f)
    }

    @Test
    fun `dynamicSnapRadiusPx should adjust based on zoom`() {
        val result1 = snappingEngine.dynamicSnapRadiusPx(1f, 16f)
        assertEquals(16f, result1, 0.001f)
        
        val result2 = snappingEngine.dynamicSnapRadiusPx(2f, 16f)
        assertEquals(8f, result2, 0.001f)
        
        val result3 = snappingEngine.dynamicSnapRadiusPx(0.5f, 16f)
        assertEquals(32f, result3, 0.001f)
    }

    @Test
    fun `dynamicSnapRadiusPx should clamp to bounds`() {
        val result1 = snappingEngine.dynamicSnapRadiusPx(0.1f, 16f)
        assertEquals(28f, result1, 0.001f)
        
        val result2 = snappingEngine.dynamicSnapRadiusPx(10f, 16f)
        assertEquals(6f, result2, 0.001f)
    }

    @Test
    fun `snapToGrid should snap to grid correctly`() {
        val point = Vec2(12.3f, 45.7f)
        val result = snappingEngine.snapToGrid(point, 5f, 160f, 1f)
        
        // Grid spacing in pixels: 5mm * 160dpi / 25.4 = ~31.5 pixels
        // Expected snap: round to nearest grid point
        assertTrue("Should snap to grid", result.distancePx < 20f)
        assertEquals("Should have grid label", "grid", result.label)
        assertEquals("Should have correct priority", 10, result.priority)
    }

    @Test
    fun `snapAngle should snap to common angles`() {
        val from = Vec2(0f, 0f)
        val to = Vec2(1f, 1f) // 45 degrees
        val (currentAngle, snappedAngle) = snappingEngine.snapAngle(from, to)
        
        assertEquals(radians(45f), snappedAngle, 0.001f)
    }

    @Test
    fun `snapAngle should snap to 0 degrees`() {
        val from = Vec2(0f, 0f)
        val to = Vec2(1f, 0.1f) // Close to 0 degrees
        val (currentAngle, snappedAngle) = snappingEngine.snapAngle(from, to)
        
        assertEquals(radians(0f), snappedAngle, 0.001f)
    }

    @Test
    fun `snapAngle should snap to 90 degrees`() {
        val from = Vec2(0f, 0f)
        val to = Vec2(0.1f, 1f) // Close to 90 degrees
        val (currentAngle, snappedAngle) = snappingEngine.snapAngle(from, to)
        
        assertEquals(radians(90f), snappedAngle, 0.001f)
    }

    @Test
    fun `snapAngle should snap to 30 degrees`() {
        val from = Vec2(0f, 0f)
        val to = Vec2(sqrt(3f), 1f) // 30 degrees
        val (currentAngle, snappedAngle) = snappingEngine.snapAngle(from, to)
        
        assertEquals(radians(30f), snappedAngle, 0.001f)
    }

    @Test
    fun `snapAngle should snap to 60 degrees`() {
        val from = Vec2(0f, 0f)
        val to = Vec2(1f, sqrt(3f)) // 60 degrees
        val (currentAngle, snappedAngle) = snappingEngine.snapAngle(from, to)
        
        assertEquals(radians(60f), snappedAngle, 0.001f)
    }

    @Test
    fun `snapAngle should handle negative angles`() {
        val from = Vec2(0f, 0f)
        val to = Vec2(1f, -1f) // -45 degrees
        val (currentAngle, snappedAngle) = snappingEngine.snapAngle(from, to)
        
        // Should snap to 315 degrees (equivalent to -45)
        assertEquals(radians(315f), snappedAngle, 0.001f)
    }

    @Test
    fun `snapAngle should handle angles greater than 180 degrees`() {
        val from = Vec2(0f, 0f)
        val to = Vec2(-1f, 1f) // 135 degrees
        val (currentAngle, snappedAngle) = snappingEngine.snapAngle(from, to)
        
        assertEquals(radians(135f), snappedAngle, 0.001f)
    }
}

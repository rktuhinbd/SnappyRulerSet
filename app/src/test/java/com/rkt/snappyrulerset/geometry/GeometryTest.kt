package com.rkt.snappyrulerset.geometry

import com.rkt.snappyrulerset.model.*
import com.rkt.snappyrulerset.snapping.*
import org.junit.Test
import org.junit.Assert.*
import kotlin.math.*

class GeometryTest {
    
    @Test
    fun testVec2Operations() {
        val a = Vec2(3f, 4f)
        val b = Vec2(1f, 2f)
        
        assertEquals(Vec2(4f, 6f), a + b)
        assertEquals(Vec2(2f, 2f), a - b)
        assertEquals(Vec2(6f, 8f), a * 2f)
        assertEquals(Vec2(1.5f, 2f), a / 2f)
    }
    
    @Test
    fun testVectorLength() {
        val v = Vec2(3f, 4f)
        assertEquals(5f, len(v), 0.001f)
    }
    
    @Test
    fun testVectorNormalization() {
        val v = Vec2(3f, 4f)
        val normalized = norm(v)
        assertEquals(1f, len(normalized), 0.001f)
    }
    
    @Test
    fun testAngleCalculations() {
        val v = Vec2(1f, 0f)
        assertEquals(0f, angleOf(v), 0.001f)
        
        val v2 = Vec2(0f, 1f)
        assertEquals(PI.toFloat() / 2f, angleOf(v2), 0.001f)
    }
    
    @Test
    fun testDistanceCalculation() {
        val a = Vec2(0f, 0f)
        val b = Vec2(3f, 4f)
        assertEquals(5f, distance(a, b), 0.001f)
    }
    
    @Test
    fun testClosestPointOnSegment() {
        val a = Vec2(0f, 0f)
        val b = Vec2(10f, 0f)
        val p = Vec2(5f, 5f)
        
        val closest = closestPointOnSegment(p, a, b)
        assertEquals(Vec2(5f, 0f), closest)
    }
    
    @Test
    fun testClosestPointOnSegmentOutside() {
        val a = Vec2(0f, 0f)
        val b = Vec2(10f, 0f)
        val p = Vec2(15f, 0f)
        
        val closest = closestPointOnSegment(p, a, b)
        assertEquals(b, closest)
    }
    
    @Test
    fun testDistancePointToSegment() {
        val a = Vec2(0f, 0f)
        val b = Vec2(10f, 0f)
        val p = Vec2(5f, 5f)
        
        val dist = distancePointToSegment(p, a, b)
        assertEquals(5f, dist, 0.001f)
    }
    
    @Test
    fun testDegreeRadianConversion() {
        assertEquals(90f, degrees(PI.toFloat() / 2f), 0.001f)
        assertEquals(PI.toFloat() / 2f, radians(90f), 0.001f)
        assertEquals(180f, degrees(PI.toFloat()), 0.001f)
        assertEquals(PI.toFloat(), radians(180f), 0.001f)
    }
    
    @Test
    fun testLinePointCollection() {
        val lines = listOf(
            Shape.Line(Vec2(0f, 0f), Vec2(10f, 0f)),
            Shape.Line(Vec2(5f, 5f), Vec2(15f, 5f))
        )
        
        val points = collectLinePoints(lines)
        assertEquals(6, points.size) // 2 endpoints + 1 midpoint per line
        
        assertTrue(points.contains(Vec2(0f, 0f)))
        assertTrue(points.contains(Vec2(10f, 0f)))
        assertTrue(points.contains(Vec2(5f, 0f))) // midpoint
        assertTrue(points.contains(Vec2(5f, 5f)))
        assertTrue(points.contains(Vec2(15f, 5f)))
        assertTrue(points.contains(Vec2(10f, 5f))) // midpoint
    }
    
    @Test
    fun testCircleCenterCollection() {
        val circles = listOf(
            Shape.Circle(Vec2(0f, 0f), 5f),
            Shape.Circle(Vec2(10f, 10f), 3f)
        )
        
        val centers = collectCircleCenters(circles)
        assertEquals(2, centers.size)
        assertTrue(centers.contains(Vec2(0f, 0f)))
        assertTrue(centers.contains(Vec2(10f, 10f)))
    }
    
    @Test
    fun testArcPointCollection() {
        val arcs = listOf(
            Shape.Arc(Vec2(0f, 0f), 5f, 0f, PI.toFloat() / 2f) // 90 degree arc
        )
        
        val points = collectArcPoints(arcs)
        assertEquals(2, points.size)
        
        // Start point should be at (5, 0)
        assertTrue(points.any { abs(it.x - 5f) < 0.001f && abs(it.y) < 0.001f })
        // End point should be at (0, 5)
        assertTrue(points.any { abs(it.x) < 0.001f && abs(it.y - 5f) < 0.001f })
    }
    
    @Test
    fun testLineIntersection() {
        val lines = listOf(
            Shape.Line(Vec2(0f, 0f), Vec2(10f, 0f)),
            Shape.Line(Vec2(5f, -5f), Vec2(5f, 5f))
        )
        
        val intersections = collectIntersections(lines)
        assertEquals(1, intersections.size)
        assertEquals(Vec2(5f, 0f), intersections[0])
    }
    
    @Test
    fun testCircleLineIntersection() {
        val circle = Shape.Circle(Vec2(0f, 0f), 5f)
        val line = Shape.Line(Vec2(-10f, 0f), Vec2(10f, 0f))
        
        val intersections = collectCircleLineIntersections(listOf(circle), listOf(line))
        assertEquals(2, intersections.size)
        
        // Should intersect at (-5, 0) and (5, 0)
        assertTrue(intersections.any { abs(it.x + 5f) < 0.001f && abs(it.y) < 0.001f })
        assertTrue(intersections.any { abs(it.x - 5f) < 0.001f && abs(it.y) < 0.001f })
    }
    
    @Test
    fun testCircleCircleIntersection() {
        val c1 = Shape.Circle(Vec2(0f, 0f), 5f)
        val c2 = Shape.Circle(Vec2(8f, 0f), 5f)
        
        val intersections = collectCircleIntersections(listOf(c1, c2))
        assertEquals(2, intersections.size)
        
        // Should intersect at (4, ±3)
        assertTrue(intersections.any { abs(it.x - 4f) < 0.001f && abs(it.y - 3f) < 0.001f })
        assertTrue(intersections.any { abs(it.x - 4f) < 0.001f && abs(it.y + 3f) < 0.001f })
    }
    
    @Test
    fun testNoIntersection() {
        val lines = listOf(
            Shape.Line(Vec2(0f, 0f), Vec2(1f, 0f)),
            Shape.Line(Vec2(0f, 1f), Vec2(1f, 1f))
        )
        
        val intersections = collectIntersections(lines)
        assertEquals(0, intersections.size)
    }
    
    @Test
    fun testSnapEngineGridSnapping() {
        val engine = SnapEngine()
        val point = Vec2(12.3f, 45.7f)
        val gridMm = 5f
        val dpi = 160f
        
        val snap = engine.snapToGrid(point, gridMm, dpi, 1f)
        
        // Should snap to nearest 5mm grid point
        val expectedSpacing = engine.mmToPx(gridMm, dpi)
        val expectedX = round(point.x / expectedSpacing) * expectedSpacing
        val expectedY = round(point.y / expectedSpacing) * expectedSpacing
        
        assertEquals(Vec2(expectedX, expectedY), snap.pos)
        assertEquals(10, snap.priority)
        assertEquals("grid", snap.label)
    }
    
    @Test
    fun testSnapEngineAngleSnapping() {
        val engine = SnapEngine()
        val from = Vec2(0f, 0f)
        val to = Vec2(1f, 1f) // 45 degree angle
        
        val (original, snapped) = engine.snapAngle(from, to)
        
        // Should snap to exactly 45 degrees (π/4)
        assertEquals(PI.toFloat() / 4f, snapped, 0.001f)
    }
    
    @Test
    fun testDynamicSnapRadius() {
        val engine = SnapEngine()
        
        // At zoom 1, should return base radius
        assertEquals(16f, engine.dynamicSnapRadiusPx(1f, 16f), 0.001f)
        
        // At zoom 2, should return half radius
        assertEquals(8f, engine.dynamicSnapRadiusPx(2f, 16f), 0.001f)
        
        // At zoom 0.5, should return double radius (but clamped)
        assertEquals(28f, engine.dynamicSnapRadiusPx(0.5f, 16f), 0.001f)
    }
}

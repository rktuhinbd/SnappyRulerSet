package com.rkt.snappyrulerset.domain.usecase

import com.rkt.snappyrulerset.domain.entity.Shape
import com.rkt.snappyrulerset.domain.entity.Vec2
import org.junit.Test
import org.junit.Assert.*

class SnapPointCollectorTest {

    @Test
    fun `collectLinePoints should collect endpoints and midpoints`() {
        val lines = listOf(
            Shape.Line(Vec2(0f, 0f), Vec2(10f, 0f)),
            Shape.Line(Vec2(0f, 0f), Vec2(0f, 10f))
        )
        
        val points = collectLinePoints(lines)
        
        assertEquals("Should have 6 points (2 lines * 3 points each)", 6, points.size)
        assertTrue("Should contain start points", points.contains(Vec2(0f, 0f)))
        assertTrue("Should contain end points", points.contains(Vec2(10f, 0f)))
        assertTrue("Should contain midpoints", points.contains(Vec2(5f, 0f)))
    }

    @Test
    fun `collectCircleCenters should collect circle centers`() {
        val circles = listOf(
            Shape.Circle(Vec2(5f, 5f), 10f),
            Shape.Circle(Vec2(10f, 10f), 15f)
        )
        
        val points = collectCircleCenters(circles)
        
        assertEquals("Should have 2 points", 2, points.size)
        assertTrue("Should contain first center", points.contains(Vec2(5f, 5f)))
        assertTrue("Should contain second center", points.contains(Vec2(10f, 10f)))
    }

    @Test
    fun `collectArcPoints should collect arc endpoints`() {
        val arcs = listOf(
            Shape.Arc(Vec2(0f, 0f), 10f, 0f, 1.57f) // 90 degree arc
        )
        
        val points = collectArcPoints(arcs)
        
        assertEquals("Should have 2 points", 2, points.size)
        // Start point should be at (10, 0), end point should be at (0, 10)
        assertTrue("Should contain start point", points.contains(Vec2(10f, 0f)))
        assertTrue("Should contain end point", points.contains(Vec2(0f, 10f)))
    }

    @Test
    fun `collectIntersections should find line intersections`() {
        val lines = listOf(
            Shape.Line(Vec2(0f, 0f), Vec2(10f, 10f)),
            Shape.Line(Vec2(0f, 10f), Vec2(10f, 0f))
        )
        
        val intersections = collectIntersections(lines)
        
        assertEquals("Should have 1 intersection", 1, intersections.size)
        assertTrue("Should contain intersection at (5, 5)", intersections.contains(Vec2(5f, 5f)))
    }

    @Test
    fun `collectIntersections should handle parallel lines`() {
        val lines = listOf(
            Shape.Line(Vec2(0f, 0f), Vec2(10f, 0f)),
            Shape.Line(Vec2(0f, 5f), Vec2(10f, 5f))
        )
        
        val intersections = collectIntersections(lines)
        
        assertTrue("Should have no intersections for parallel lines", intersections.isEmpty())
    }

    @Test
    fun `collectIntersections should handle non-intersecting lines`() {
        val lines = listOf(
            Shape.Line(Vec2(0f, 0f), Vec2(5f, 0f)),
            Shape.Line(Vec2(0f, 10f), Vec2(5f, 10f))
        )
        
        val intersections = collectIntersections(lines)
        
        assertTrue("Should have no intersections for non-intersecting lines", intersections.isEmpty())
    }

    @Test
    fun `collectCircleLineIntersections should find circle-line intersections`() {
        val circles = listOf(
            Shape.Circle(Vec2(0f, 0f), 5f)
        )
        val lines = listOf(
            Shape.Line(Vec2(-10f, 0f), Vec2(10f, 0f))
        )
        
        val intersections = collectCircleLineIntersections(circles, lines)
        
        assertEquals("Should have 2 intersections", 2, intersections.size)
        assertTrue("Should contain intersection at (-5, 0)", intersections.contains(Vec2(-5f, 0f)))
        assertTrue("Should contain intersection at (5, 0)", intersections.contains(Vec2(5f, 0f)))
    }

    @Test
    fun `collectCircleLineIntersections should handle tangent lines`() {
        val circles = listOf(
            Shape.Circle(Vec2(0f, 0f), 5f)
        )
        val lines = listOf(
            Shape.Line(Vec2(0f, 5f), Vec2(10f, 5f)) // Tangent line
        )
        
        val intersections = collectCircleLineIntersections(circles, lines)
        
        assertEquals("Should have 1 intersection for tangent line", 1, intersections.size)
        assertTrue("Should contain tangent point at (0, 5)", intersections.contains(Vec2(0f, 5f)))
    }

    @Test
    fun `collectCircleIntersections should find circle intersections`() {
        val circles = listOf(
            Shape.Circle(Vec2(0f, 0f), 5f),
            Shape.Circle(Vec2(8f, 0f), 5f)
        )
        
        val intersections = collectCircleIntersections(circles)
        
        assertEquals("Should have 2 intersections", 2, intersections.size)
        // Intersections should be at (4, ±3) approximately
        assertTrue("Should contain intersection points", intersections.size == 2)
    }

    @Test
    fun `collectCircleIntersections should handle non-intersecting circles`() {
        val circles = listOf(
            Shape.Circle(Vec2(0f, 0f), 2f),
            Shape.Circle(Vec2(10f, 0f), 2f)
        )
        
        val intersections = collectCircleIntersections(circles)
        
        assertTrue("Should have no intersections for non-intersecting circles", intersections.isEmpty())
    }

    @Test
    fun `collectCircleIntersections should handle tangent circles`() {
        val circles = listOf(
            Shape.Circle(Vec2(0f, 0f), 3f),
            Shape.Circle(Vec2(6f, 0f), 3f)
        )
        
        val intersections = collectCircleIntersections(circles)
        
        assertEquals("Should have 1 intersection for tangent circles", 1, intersections.size)
        assertTrue("Should contain tangent point at (3, 0)", intersections.contains(Vec2(3f, 0f)))
    }

    @Test
    fun `collectCircleIntersections should handle concentric circles`() {
        val circles = listOf(
            Shape.Circle(Vec2(0f, 0f), 3f),
            Shape.Circle(Vec2(0f, 0f), 5f)
        )
        
        val intersections = collectCircleIntersections(circles)
        
        assertTrue("Should have no intersections for concentric circles", intersections.isEmpty())
    }

    @Test
    fun `collectLinePoints should handle empty list`() {
        val points = collectLinePoints(emptyList())
        assertTrue("Should return empty list for empty input", points.isEmpty())
    }

    @Test
    fun `collectCircleCenters should handle empty list`() {
        val points = collectCircleCenters(emptyList())
        assertTrue("Should return empty list for empty input", points.isEmpty())
    }

    @Test
    fun `collectArcPoints should handle empty list`() {
        val points = collectArcPoints(emptyList())
        assertTrue("Should return empty list for empty input", points.isEmpty())
    }
}

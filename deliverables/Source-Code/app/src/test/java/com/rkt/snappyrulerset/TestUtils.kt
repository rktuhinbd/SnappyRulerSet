package com.rkt.snappyrulerset

import com.rkt.snappyrulerset.domain.entity.*
import org.junit.Assert.*

/**
 * Test utilities for creating test data and assertions
 */
object TestUtils {
    
    /**
     * Creates a test line with given start and end points
     */
    fun createTestLine(startX: Float, startY: Float, endX: Float, endY: Float): Shape.Line {
        return Shape.Line(Vec2(startX, startY), Vec2(endX, endY))
    }
    
    /**
     * Creates a test circle with given center and radius
     */
    fun createTestCircle(centerX: Float, centerY: Float, radius: Float): Shape.Circle {
        return Shape.Circle(Vec2(centerX, centerY), radius)
    }
    
    /**
     * Creates a test point with given coordinates
     */
    fun createTestPoint(x: Float, y: Float): Shape.Point {
        return Shape.Point(Vec2(x, y))
    }
    
    /**
     * Creates a test arc with given parameters
     */
    fun createTestArc(centerX: Float, centerY: Float, radius: Float, startRad: Float, sweepRad: Float): Shape.Arc {
        return Shape.Arc(Vec2(centerX, centerY), radius, startRad, sweepRad)
    }
    
    /**
     * Creates a test drawing state with given shapes
     */
    fun createTestDrawingState(shapes: List<Shape> = emptyList()): DrawingState {
        return DrawingState(shapes = shapes)
    }
    
    /**
     * Creates a test viewport with given pan and zoom
     */
    fun createTestViewport(panX: Float = 0f, panY: Float = 0f, zoom: Float = 1f): Viewport {
        return Viewport(pan = Vec2(panX, panY), zoom = zoom)
    }
    
    /**
     * Creates a test tool state with given parameters
     */
    fun createTestToolState(
        kind: ToolKind = ToolKind.Ruler,
        positionX: Float = 0f,
        positionY: Float = 0f,
        rotationRad: Float = 0f,
        scale: Float = 1f
    ): ToolState {
        return ToolState(
            kind = kind,
            transform = ToolTransform(
                position = Vec2(positionX, positionY),
                rotationRad = rotationRad,
                scale = scale
            )
        )
    }
    
    /**
     * Asserts that two Vec2 objects are approximately equal within tolerance
     */
    fun assertVec2Equals(expected: Vec2, actual: Vec2, tolerance: Float = 0.001f) {
        assertEquals("X coordinate mismatch", expected.x, actual.x, tolerance)
        assertEquals("Y coordinate mismatch", expected.y, actual.y, tolerance)
    }
    
    /**
     * Asserts that a list contains a specific shape
     */
    fun assertContainsShape(shapes: List<Shape>, expectedShape: Shape) {
        assertTrue("Shape list should contain expected shape", shapes.contains(expectedShape))
    }
    
    /**
     * Asserts that a list does not contain a specific shape
     */
    fun assertNotContainsShape(shapes: List<Shape>, unexpectedShape: Shape) {
        assertFalse("Shape list should not contain unexpected shape", shapes.contains(unexpectedShape))
    }
    
    /**
     * Asserts that two drawing states are equal
     */
    fun assertDrawingStateEquals(expected: DrawingState, actual: DrawingState) {
        assertEquals("Shapes should match", expected.shapes, actual.shapes)
        assertEquals("Tool should match", expected.tool, actual.tool)
        assertEquals("Viewport should match", expected.viewport, actual.viewport)
        assertEquals("Snapping should match", expected.snapping, actual.snapping)
        assertEquals("Grid spacing should match", expected.gridSpacingMm, actual.gridSpacingMm, 0.001f)
        assertEquals("Snap radius should match", expected.snapRadiusPx, actual.snapRadiusPx, 0.001f)
    }
    
    /**
     * Creates a list of test lines forming a grid pattern
     */
    fun createGridLines(rows: Int, cols: Int, spacing: Float): List<Shape.Line> {
        val lines = mutableListOf<Shape.Line>()
        
        // Horizontal lines
        for (row in 0..rows) {
            val y = row * spacing
            lines.add(createTestLine(0f, y, cols * spacing, y))
        }
        
        // Vertical lines
        for (col in 0..cols) {
            val x = col * spacing
            lines.add(createTestLine(x, 0f, x, rows * spacing))
        }
        
        return lines
    }
    
    /**
     * Creates a list of test circles in a pattern
     */
    fun createCirclePattern(rows: Int, cols: Int, spacing: Float, radius: Float): List<Shape.Circle> {
        val circles = mutableListOf<Shape.Circle>()
        
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val x = col * spacing
                val y = row * spacing
                circles.add(createTestCircle(x, y, radius))
            }
        }
        
        return circles
    }
}

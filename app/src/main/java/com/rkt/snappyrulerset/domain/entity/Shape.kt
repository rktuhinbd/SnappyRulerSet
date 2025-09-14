package com.rkt.snappyrulerset.domain.entity

/**
 * Represents different types of geometric shapes that can be drawn on the canvas.
 * Uses a sealed class to ensure type safety and exhaustive pattern matching.
 */
sealed class Shape {
    /** A straight line segment between two points */
    data class Line(val a: Vec2, val b: Vec2): Shape()
    
    /** A circle with a center point and radius */
    data class Circle(val center: Vec2, val r: Float): Shape()
    
    /** An arc (portion of a circle) with center, radius, start angle, and sweep angle */
    data class Arc(val center: Vec2, val r: Float, val startRad: Float, val sweepRad: Float): Shape()
    
    /** A single point */
    data class Point(val p: Vec2): Shape()
    
    /** A path consisting of multiple connected points */
    data class Path(val points: List<Vec2>): Shape()
}
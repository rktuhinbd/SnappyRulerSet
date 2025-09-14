package com.rkt.snappyrulerset

import com.rkt.snappyrulerset.domain.entity.Vec2
import com.rkt.snappyrulerset.domain.entity.Shape
import org.junit.BeforeClass
import org.junit.AfterClass

/**
 * Test configuration and utilities for the Snappy Ruler Set test suite.
 * Provides common test data, constants, and setup/teardown methods.
 */
object TestConfiguration {
    
    // Test constants
    const val TEST_DPI = 160f
    const val TEST_GRID_SPACING_MM = 5f
    const val TEST_ZOOM_LEVEL = 1f
    const val TEST_SNAP_RADIUS = 16f
    
    // Performance test thresholds
    const val MAX_INSERTION_TIME_MS = 1000L
    const val MAX_QUERY_TIME_MS = 100L
    const val MAX_INTERSECTION_TIME_MS = 500L
    const val MAX_MEMORY_USAGE_MB = 10L
    
    // Test data generators
    fun generateTestPoints(count: Int): List<Vec2> {
        return (0 until count).map { i ->
            Vec2(i.toFloat(), i.toFloat())
        }
    }
    
    fun generateTestLines(count: Int): List<Shape.Line> {
        return (0 until count).map { i ->
            Shape.Line(
                Vec2(i.toFloat(), i.toFloat()),
                Vec2((i + 10).toFloat(), (i + 10).toFloat())
            )
        }
    }
    
    fun generateTestCircles(count: Int): List<Shape.Circle> {
        return (0 until count).map { i ->
            Shape.Circle(
                Vec2(i.toFloat(), i.toFloat()),
                (i + 1).toFloat()
            )
        }
    }
    
    // Utility functions
    fun measureTime(operation: () -> Unit): Long {
        val startTime = System.currentTimeMillis()
        operation()
        return System.currentTimeMillis() - startTime
    }
    
    fun measureMemory(operation: () -> Unit): Long {
        val beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        operation()
        val afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        return afterMemory - beforeMemory
    }
    
    fun printPerformanceResults(
        operation: String,
        count: Int,
        timeMs: Long,
        additionalInfo: String = ""
    ) {
        println("Performance Results:")
        println("Operation: $operation")
        println("Count: $count")
        println("Time: ${timeMs}ms")
        println("Average time per item: ${timeMs.toFloat() / count}ms")
        if (additionalInfo.isNotEmpty()) {
            println("Additional info: $additionalInfo")
        }
        println("---")
    }
}

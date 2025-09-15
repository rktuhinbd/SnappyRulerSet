package com.rkt.snappyrulerset.performance

import com.rkt.snappyrulerset.domain.entity.Shape
import com.rkt.snappyrulerset.domain.entity.Vec2
import com.rkt.snappyrulerset.domain.usecase.SpatialGrid
import com.rkt.snappyrulerset.domain.usecase.collectLinePoints
import com.rkt.snappyrulerset.domain.usecase.collectIntersections
import com.rkt.snappyrulerset.TestConfiguration
import org.junit.Test
import org.junit.Assert.*

class PerformanceTest {

    @Test
    fun `spatial grid should handle large number of points efficiently`() {
        val grid = SpatialGrid(64f)
        val numPoints = 10000
        
        // Measure insertion time
        val insertionTime = TestConfiguration.measureTime {
            repeat(numPoints) { i ->
                grid.insert(Vec2(i.toFloat(), i.toFloat()))
            }
        }
        
        // Measure query time
        val queryTime = TestConfiguration.measureTime {
            repeat(1000) { i ->
                grid.queryNear(Vec2(i.toFloat(), i.toFloat()), 20)
            }
        }
        
        // Performance assertions
        assertTrue("Insertion should be fast (< 1000ms for 10k points)", insertionTime < TestConfiguration.MAX_INSERTION_TIME_MS)
        assertTrue("Query should be fast (< 100ms for 1k queries)", queryTime < TestConfiguration.MAX_QUERY_TIME_MS)
        
        TestConfiguration.printPerformanceResults("Spatial Grid Operations", numPoints, insertionTime, "Query time: ${queryTime}ms")
    }

    @Test
    fun `line intersection calculation should be efficient`() {
        val numLines = 1000
        val lines = (0 until numLines).map { i ->
            Shape.Line(
                Vec2(i.toFloat(), i.toFloat()),
                Vec2((i + 10).toFloat(), (i + 10).toFloat())
            )
        }
        
        val processingTime = TestConfiguration.measureTime {
            collectLinePoints(lines)
        }
        
        // Performance assertion
        assertTrue("Line point collection should be fast (< 500ms for 1k lines)", processingTime < TestConfiguration.MAX_INTERSECTION_TIME_MS)
        
        TestConfiguration.printPerformanceResults("Line Point Collection", numLines, processingTime)
    }

    @Test
    fun `intersection detection should scale reasonably`() {
        val numLines = 100
        val lines = (0 until numLines).map { i ->
            Shape.Line(
                Vec2(i.toFloat(), 0f),
                Vec2(i.toFloat(), 100f)
            )
        }
        
        val processingTime = TestConfiguration.measureTime {
            collectIntersections(lines)
        }
        
        // Performance assertion - O(n²) algorithm should still be reasonable for small n
        assertTrue("Intersection detection should be fast (< 1000ms for 100 lines)", processingTime < 1000)
        
        TestConfiguration.printPerformanceResults("Intersection Detection", numLines, processingTime)
    }

    @Test
    fun `spatial grid query performance should be consistent`() {
        val grid = SpatialGrid(64f)
        val numPoints = 5000
        
        // Insert points
        repeat(numPoints) { i ->
            grid.insert(Vec2(i.toFloat(), i.toFloat()))
        }
        
        val queryTimes = mutableListOf<Long>()
        val numQueries = 100
        
        repeat(numQueries) { i ->
            val startTime = System.nanoTime()
            grid.queryNear(Vec2(i.toFloat(), i.toFloat()), 20)
            val endTime = System.nanoTime()
            queryTimes.add(endTime - startTime)
        }
        
        val averageQueryTime = queryTimes.average() / 1_000_000.0 // Convert to milliseconds
        val maxQueryTime = queryTimes.maxOrNull()!! / 1_000_000.0
        val minQueryTime = queryTimes.minOrNull()!! / 1_000_000.0
        
        // Performance assertions
        assertTrue("Average query time should be fast (< 1ms)", averageQueryTime < 1.0)
        assertTrue("Max query time should be reasonable (< 5ms)", maxQueryTime < 5.0)
        
        println("Performance Results:")
        println("Average query time: ${String.format("%.3f", averageQueryTime)}ms")
        println("Max query time: ${String.format("%.3f", maxQueryTime)}ms")
        println("Min query time: ${String.format("%.3f", minQueryTime)}ms")
    }

    @Test
    fun `memory usage should be reasonable for large datasets`() {
        val grid = SpatialGrid(64f)
        val numPoints = 10000
        
        val memoryUsed = TestConfiguration.measureMemory {
            repeat(numPoints) { i ->
                grid.insert(Vec2(i.toFloat(), i.toFloat()))
            }
        }
        
        // Memory assertion - should use less than 10MB for 10k points
        assertTrue("Memory usage should be reasonable (< 10MB for 10k points)", memoryUsed < TestConfiguration.MAX_MEMORY_USAGE_MB * 1024 * 1024)
        
        println("Performance Results:")
        println("Memory used for $numPoints points: ${memoryUsed / 1024}KB")
        println("Average memory per point: ${memoryUsed / numPoints} bytes")
    }

    @Test
    fun `grid cell distribution should be balanced`() {
        val grid = SpatialGrid(64f)
        val numPoints = 1000
        
        // Insert points in a grid pattern
        val gridSize = kotlin.math.sqrt(numPoints.toFloat()).toInt()
        repeat(gridSize) { x ->
            repeat(gridSize) { y ->
                grid.insert(Vec2(x * 64f, y * 64f))
            }
        }
        
        // Count points in each cell
        val cellCounts = mutableMapOf<Long, Int>()
        repeat(gridSize) { x ->
            repeat(gridSize) { y ->
                val point = Vec2(x * 64f, y * 64f)
                val nearby = grid.queryNear(point, 100)
                cellCounts[point.hashCode().toLong()] = nearby.size
            }
        }
        
        val averagePointsPerCell = cellCounts.values.average()
        val maxPointsPerCell = cellCounts.values.maxOrNull()!!
        val minPointsPerCell = cellCounts.values.minOrNull()!!
        
        // Distribution assertion
        assertTrue("Cell distribution should be balanced", maxPointsPerCell - minPointsPerCell <= 2)
        
        println("Performance Results:")
        println("Average points per cell: ${String.format("%.2f", averagePointsPerCell)}")
        println("Max points per cell: $maxPointsPerCell")
        println("Min points per cell: $minPointsPerCell")
    }
}

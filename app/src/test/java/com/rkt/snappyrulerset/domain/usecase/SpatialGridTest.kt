package com.rkt.snappyrulerset.domain.usecase

import com.rkt.snappyrulerset.domain.entity.Vec2
import org.junit.Test
import org.junit.Assert.*

class SpatialGridTest {

    @Test
    fun `insert should add point to grid`() {
        val grid = SpatialGrid(64f)
        val point = Vec2(100f, 100f)
        
        grid.insert(point)
        
        val nearby = grid.queryNear(point, 10)
        assertTrue("Should find inserted point", nearby.contains(point))
    }

    @Test
    fun `insertAll should add multiple points`() {
        val grid = SpatialGrid(64f)
        val points = listOf(
            Vec2(100f, 100f),
            Vec2(200f, 200f),
            Vec2(300f, 300f)
        )
        
        grid.insertAll(points)
        
        points.forEach { point ->
            val nearby = grid.queryNear(point, 10)
            assertTrue("Should find point $point", nearby.contains(point))
        }
    }

    @Test
    fun `queryNear should find points in adjacent cells`() {
        val grid = SpatialGrid(64f)
        val point1 = Vec2(100f, 100f)
        val point2 = Vec2(150f, 150f) // In adjacent cell
        
        grid.insert(point1)
        grid.insert(point2)
        
        val nearby = grid.queryNear(point1, 10)
        assertTrue("Should find both points", nearby.contains(point1) && nearby.contains(point2))
    }

    @Test
    fun `queryNear should respect maxResults limit`() {
        val grid = SpatialGrid(64f)
        val center = Vec2(100f, 100f)
        
        // Insert more points than maxResults
        repeat(50) { i ->
            grid.insert(Vec2(100f + i, 100f + i))
        }
        
        val nearby = grid.queryNear(center, 10)
        assertTrue("Should respect maxResults limit", nearby.size <= 10)
    }

    @Test
    fun `queryNear should not find distant points`() {
        val grid = SpatialGrid(64f)
        val point1 = Vec2(100f, 100f)
        val point2 = Vec2(1000f, 1000f) // Far away
        
        grid.insert(point1)
        grid.insert(point2)
        
        val nearby = grid.queryNear(point1, 10)
        assertTrue("Should not find distant point", !nearby.contains(point2))
    }

    @Test
    fun `clear should remove all points`() {
        val grid = SpatialGrid(64f)
        val point = Vec2(100f, 100f)
        
        grid.insert(point)
        assertTrue("Should find point before clear", grid.queryNear(point, 10).contains(point))
        
        grid.clear()
        assertTrue("Should not find point after clear", !grid.queryNear(point, 10).contains(point))
    }

    @Test
    fun `queryNear should handle empty grid`() {
        val grid = SpatialGrid(64f)
        val point = Vec2(100f, 100f)
        
        val nearby = grid.queryNear(point, 10)
        assertTrue("Should return empty list for empty grid", nearby.isEmpty())
    }

    @Test
    fun `queryNear should handle points at cell boundaries`() {
        val grid = SpatialGrid(64f)
        val point1 = Vec2(64f, 64f) // At cell boundary
        val point2 = Vec2(65f, 65f) // Just across boundary
        
        grid.insert(point1)
        grid.insert(point2)
        
        val nearby1 = grid.queryNear(point1, 10)
        val nearby2 = grid.queryNear(point2, 10)
        
        assertTrue("Should find both points from either query", 
            (nearby1.contains(point1) && nearby1.contains(point2)) ||
            (nearby2.contains(point1) && nearby2.contains(point2)))
    }

    @Test
    fun `queryNear should handle negative coordinates`() {
        val grid = SpatialGrid(64f)
        val point = Vec2(-100f, -100f)
        
        grid.insert(point)
        
        val nearby = grid.queryNear(point, 10)
        assertTrue("Should find point with negative coordinates", nearby.contains(point))
    }

    @Test
    fun `queryNear should handle zero coordinates`() {
        val grid = SpatialGrid(64f)
        val point = Vec2(0f, 0f)
        
        grid.insert(point)
        
        val nearby = grid.queryNear(point, 10)
        assertTrue("Should find point at origin", nearby.contains(point))
    }
}

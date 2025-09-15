package com.rkt.snappyrulerset.domain.usecase

import com.rkt.snappyrulerset.domain.entity.Vec2
import kotlin.math.floor

/**
 * A spatial grid for efficient point queries.
 * Divides 2D space into a grid of cells to optimize nearest neighbor searches.
 * 
 * @param cell The size of each grid cell in pixels (default: 64f)
 */
class SpatialGrid(private val cell: Float = 64f) {
    private val map = HashMap<Long, MutableList<Vec2>>()

    private fun key(x: Float, y: Float): Long {
        val cx = floor(x / cell).toLong()
        val cy = floor(y / cell).toLong()
        return (cx shl 32) xor (cy and 0xffffffffL)
    }

    /**
     * Clears all points from the grid
     */
    fun clear() = map.clear()

    /**
     * Inserts a single point into the grid
     */
    fun insert(p: Vec2) {
        map.getOrPut(key(p.x, p.y)) { mutableListOf() }.add(p)
    }

    /**
     * Inserts multiple points into the grid
     */
    fun insertAll(points: Iterable<Vec2>) { points.forEach { insert(it) } }

    /**
     * Queries for points near the given position within adjacent grid cells
     * @param p The center point for the query
     * @param maxResults Maximum number of results to return
     * @return List of nearby points
     */
    fun queryNear(p: Vec2, maxResults: Int = 20): List<Vec2> {
        val cx = floor(p.x / cell).toLong()
        val cy = floor(p.y / cell).toLong()
        val out = ArrayList<Vec2>(maxResults)
        for (dy in -1..1) for (dx in -1..1) {
            if (out.size >= maxResults) break
            val k = ((cx + dx) shl 32) xor ((cy + dy) and 0xffffffffL)
            map[k]?.let { 
                for (point in it) {
                    if (out.size >= maxResults) break
                    out.add(point)
                }
            }
        }
        return out
    }
}
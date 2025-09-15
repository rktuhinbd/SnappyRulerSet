package com.rkt.snappyrulerset.domain.entity

import org.junit.Test
import org.junit.Assert.*
import kotlin.math.*

class GeometryTest {

    @Test
    fun `Vec2 addition should work correctly`() {
        val v1 = Vec2(3f, 4f)
        val v2 = Vec2(1f, 2f)
        val result = v1 + v2
        assertEquals(Vec2(4f, 6f), result)
    }

    @Test
    fun `Vec2 subtraction should work correctly`() {
        val v1 = Vec2(5f, 7f)
        val v2 = Vec2(2f, 3f)
        val result = v1 - v2
        assertEquals(Vec2(3f, 4f), result)
    }

    @Test
    fun `Vec2 scalar multiplication should work correctly`() {
        val v = Vec2(3f, 4f)
        val result = v * 2f
        assertEquals(Vec2(6f, 8f), result)
    }

    @Test
    fun `Vec2 scalar division should work correctly`() {
        val v = Vec2(8f, 6f)
        val result = v / 2f
        assertEquals(Vec2(4f, 3f), result)
    }

    @Test
    fun `dot product should calculate correctly`() {
        val v1 = Vec2(3f, 4f)
        val v2 = Vec2(1f, 2f)
        val result = dot(v1, v2)
        assertEquals(11f, result, 0.001f)
    }

    @Test
    fun `len should calculate correctly`() {
        val v = Vec2(3f, 4f)
        val result = len(v)
        assertEquals(5f, result, 0.001f)
    }

    @Test
    fun `norm should return unit vector`() {
        val v = Vec2(3f, 4f)
        val result = norm(v)
        val expected = Vec2(0.6f, 0.8f)
        assertEquals(expected.x, result.x, 0.001f)
        assertEquals(expected.y, result.y, 0.001f)
        assertEquals(1f, len(result), 0.001f)
    }

    @Test
    fun `norm should handle zero vector`() {
        val v = Vec2(0f, 0f)
        val result = norm(v)
        assertEquals(v, result)
    }

    @Test
    fun `angleOf should calculate correctly`() {
        val v = Vec2(1f, 0f)
        val result = angleOf(v)
        assertEquals(0f, result, 0.001f)
        
        val v2 = Vec2(0f, 1f)
        val result2 = angleOf(v2)
        assertEquals(PI.toFloat() / 2f, result2, 0.001f)
    }

    @Test
    fun `fromAngle should create correct vector`() {
        val result = fromAngle(0f)
        assertEquals(Vec2(1f, 0f), result)
        
        val result2 = fromAngle(PI.toFloat() / 2f)
        assertEquals(Vec2(0f, 1f).x, result2.x, 0.001f)
        assertEquals(Vec2(0f, 1f).y, result2.y, 0.001f)
    }

    @Test
    fun `degrees should convert correctly`() {
        val result = degrees(PI.toFloat())
        assertEquals(180f, result, 0.001f)
        
        val result2 = degrees(PI.toFloat() / 2f)
        assertEquals(90f, result2, 0.001f)
    }

    @Test
    fun `radians should convert correctly`() {
        val result = radians(180f)
        assertEquals(PI.toFloat(), result, 0.001f)
        
        val result2 = radians(90f)
        assertEquals(PI.toFloat() / 2f, result2, 0.001f)
    }

    @Test
    fun `distance should calculate correctly`() {
        val p1 = Vec2(0f, 0f)
        val p2 = Vec2(3f, 4f)
        val result = distance(p1, p2)
        assertEquals(5f, result, 0.001f)
    }

    @Test
    fun `closestPointOnSegment should project correctly`() {
        val point = Vec2(1f, 1f)
        val segmentStart = Vec2(0f, 0f)
        val segmentEnd = Vec2(2f, 0f)
        val result = closestPointOnSegment(point, segmentStart, segmentEnd)
        assertEquals(Vec2(1f, 0f), result)
    }

    @Test
    fun `closestPointOnSegment should clamp to segment endpoints`() {
        val point = Vec2(-1f, 0f)
        val segmentStart = Vec2(0f, 0f)
        val segmentEnd = Vec2(2f, 0f)
        val result = closestPointOnSegment(point, segmentStart, segmentEnd)
        assertEquals(segmentStart, result)
        
        val point2 = Vec2(3f, 0f)
        val result2 = closestPointOnSegment(point2, segmentStart, segmentEnd)
        assertEquals(segmentEnd, result2)
    }

    @Test
    fun `closestPointOnSegment should handle zero-length segment`() {
        val point = Vec2(1f, 1f)
        val segmentStart = Vec2(0f, 0f)
        val segmentEnd = Vec2(0f, 0f)
        val result = closestPointOnSegment(point, segmentStart, segmentEnd)
        assertEquals(segmentStart, result)
    }

    @Test
    fun `distancePointToSegment should calculate correctly`() {
        val point = Vec2(1f, 1f)
        val segmentStart = Vec2(0f, 0f)
        val segmentEnd = Vec2(2f, 0f)
        val result = distancePointToSegment(point, segmentStart, segmentEnd)
        assertEquals(1f, result, 0.001f)
    }

    @Test
    fun `distancePointToSegment should handle point on segment`() {
        val point = Vec2(1f, 0f)
        val segmentStart = Vec2(0f, 0f)
        val segmentEnd = Vec2(2f, 0f)
        val result = distancePointToSegment(point, segmentStart, segmentEnd)
        assertEquals(0f, result, 0.001f)
    }
}

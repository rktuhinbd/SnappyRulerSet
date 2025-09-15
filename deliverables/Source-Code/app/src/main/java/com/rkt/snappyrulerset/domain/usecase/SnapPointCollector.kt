package com.rkt.snappyrulerset.domain.usecase

import com.rkt.snappyrulerset.domain.entity.Shape
import com.rkt.snappyrulerset.domain.entity.Vec2
import com.rkt.snappyrulerset.domain.entity.distance
import com.rkt.snappyrulerset.domain.entity.fromAngle
import com.rkt.snappyrulerset.domain.entity.minus
import com.rkt.snappyrulerset.domain.entity.plus
import com.rkt.snappyrulerset.domain.entity.times
import kotlin.math.abs
import kotlin.math.sqrt

// Endpoints + midpoints from existing lines + circle centers
fun collectLinePoints(lines: List<Shape.Line>): List<Vec2> {
    val pts = ArrayList<Vec2>(lines.size * 3)
    lines.forEach { l ->
        pts.add(l.a)
        pts.add(l.b)
        pts.add(Vec2((l.a.x + l.b.x) / 2f, (l.a.y + l.b.y) / 2f))
    }
    return pts
}

// Circle centers for snapping
fun collectCircleCenters(circles: List<Shape.Circle>): List<Vec2> {
    return circles.map { it.center }
}

// Arc endpoints for snapping
fun collectArcPoints(arcs: List<Shape.Arc>): List<Vec2> {
    val pts = ArrayList<Vec2>(arcs.size * 2)
    arcs.forEach { arc ->
        val startDir = fromAngle(arc.startRad)
        val endDir = fromAngle(arc.startRad + arc.sweepRad)
        pts.add(arc.center + startDir * arc.r)
        pts.add(arc.center + endDir * arc.r)
    }
    return pts
}

// Pairwise intersections (simple O(n^2); fine for small drawings)
fun collectIntersections(lines: List<Shape.Line>): List<Vec2> {
    fun ccw(a: Vec2, b: Vec2, c: Vec2) = (c.y - a.y) * (b.x - a.x) > (b.y - a.y) * (c.x - a.x)
    fun segsIntersect(a: Vec2, b: Vec2, c: Vec2, d: Vec2): Boolean =
        ccw(a, c, d) != ccw(b, c, d) && ccw(a, b, c) != ccw(a, b, d)

    fun intersection(a: Vec2, b: Vec2, c: Vec2, d: Vec2): Vec2? {
        val x1 = a.x; val y1 = a.y; val x2 = b.x; val y2 = b.y
        val x3 = c.x; val y3 = c.y; val x4 = d.x; val y4 = d.y
        val denom = (x1-x2)*(y3-y4) - (y1-y2)*(x3-x4)
        if (abs(denom) < 1e-4f) return null
        val px = ((x1*y2 - y1*x2)*(x3-x4) - (x1-x2)*(x3*y4 - y3*x4)) / denom
        val py = ((x1*y2 - y1*x2)*(y3-y4) - (y1-y2)*(x3*y4 - y3*x4)) / denom
        return Vec2(px, py)
    }

    val out = ArrayList<Vec2>()
    for (i in 0 until lines.size) {
        for (j in i+1 until lines.size) {
            val l1 = lines[i]; val l2 = lines[j]
            if (segsIntersect(l1.a, l1.b, l2.a, l2.b)) {
                intersection(l1.a, l1.b, l2.a, l2.b)?.let { out.add(it) }
            }
        }
    }
    return out
}

// Circle-line intersections
fun collectCircleLineIntersections(circles: List<Shape.Circle>, lines: List<Shape.Line>): List<Vec2> {
    val out = ArrayList<Vec2>()
    circles.forEach { circle ->
        lines.forEach { line ->
            val intersections = circleLineIntersection(circle, line)
            out.addAll(intersections)
        }
    }
    return out
}

// Circle-circle intersections
fun collectCircleIntersections(circles: List<Shape.Circle>): List<Vec2> {
    val out = ArrayList<Vec2>()
    for (i in 0 until circles.size) {
        for (j in i + 1 until circles.size) {
            val intersections = circleCircleIntersection(circles[i], circles[j])
            out.addAll(intersections)
        }
    }
    return out
}

private fun circleLineIntersection(circle: Shape.Circle, line: Shape.Line): List<Vec2> {
    val dx = line.b.x - line.a.x
    val dy = line.b.y - line.a.y
    val dr = sqrt(dx * dx + dy * dy)
    if (dr == 0f) return emptyList()
    
    val discriminant = circle.r * circle.r * dr * dr - 
        (circle.center.x * dy - circle.center.y * dx) * (circle.center.x * dy - circle.center.y * dx)
    
    if (discriminant < 0) return emptyList()
    
    val x1 = (circle.center.x * dy * dy + dx * (circle.center.y * dx + dr * sqrt(discriminant))) / (dr * dr)
    val y1 = (circle.center.y * dx * dx + dy * (circle.center.x * dy - dr * sqrt(discriminant))) / (dr * dr)
    val x2 = (circle.center.x * dy * dy + dx * (circle.center.y * dx - dr * sqrt(discriminant))) / (dr * dr)
    val y2 = (circle.center.y * dx * dx + dy * (circle.center.x * dy + dr * sqrt(discriminant))) / (dr * dr)
    
    val result = ArrayList<Vec2>()
    val p1 = Vec2(x1, y1)
    val p2 = Vec2(x2, y2)
    
    // Check if points are on the line segment
    if (isPointOnSegment(p1, line.a, line.b)) result.add(p1)
    if (isPointOnSegment(p2, line.a, line.b)) result.add(p2)
    
    return result
}

private fun circleCircleIntersection(c1: Shape.Circle, c2: Shape.Circle): List<Vec2> {
    val d = distance(c1.center, c2.center)
    if (d > c1.r + c2.r || d < abs(c1.r - c2.r) || d == 0f) return emptyList()
    
    val a = (c1.r * c1.r - c2.r * c2.r + d * d) / (2 * d)
    val h = sqrt(c1.r * c1.r - a * a)
    
    val p2 = c1.center + (c2.center - c1.center) * (a / d)
    val x3 = p2.x + h * (c2.center.y - c1.center.y) / d
    val y3 = p2.y - h * (c2.center.x - c1.center.x) / d
    val x4 = p2.x - h * (c2.center.y - c1.center.y) / d
    val y4 = p2.y + h * (c2.center.x - c1.center.x) / d
    
    return listOf(Vec2(x3, y3), Vec2(x4, y4))
}

private fun isPointOnSegment(p: Vec2, a: Vec2, b: Vec2): Boolean {
    val cross = (p.y - a.y) * (b.x - a.x) - (p.x - a.x) * (b.y - a.y)
    if (abs(cross) > 1e-4f) return false
    
    val dot = (p.x - a.x) * (b.x - a.x) + (p.y - a.y) * (b.y - a.y)
    val lenSq = (b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y)
    
    return dot >= 0 && dot <= lenSq
}
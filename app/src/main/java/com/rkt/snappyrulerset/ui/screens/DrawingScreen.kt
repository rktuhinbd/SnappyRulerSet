package com.rkt.snappyrulerset.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rkt.snappyrulerset.model.*
import com.rkt.snappyrulerset.snapping.*
import com.rkt.snappyrulerset.performance.PerformanceMonitor
import com.rkt.snappyrulerset.calibration.CalibrationManager
import com.rkt.snappyrulerset.ui.theme.SnappyRulerSetTheme
import java.io.File
import java.util.Locale
import kotlin.math.*

// Modes: Hand = pan; Line = free lines (snap to common angles); RulerLine = along ruler edge
// SquareLine = along nearest set-square edge; Tool = drag/rotate active tool
private enum class Mode { Move, Pencil, Line, RulerLine, SquareLine, Protractor, Compass, Tool }

// Active tool choice
private enum class ToolKind { Ruler, SetSquare45, SetSquare30_60, Protractor, Compass }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingScreen(vm: com.rkt.snappyrulerset.viewmodel.DrawingViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val calibrationManager = remember { CalibrationManager(context) }
    val calibrationData = remember { calibrationManager.getCalibrationData() }
    val performanceMonitor = remember { PerformanceMonitor() }

    val dpi = calibrationData.dpi
    val gridMm = 5f // 5mm grid spacing
    val mmPerPx = calibrationData.mmPerPx
    val haptic = LocalHapticFeedback.current

    // Performance monitoring
    LaunchedEffect(Unit) {
        while (true) {
            performanceMonitor.startFrame()
            kotlinx.coroutines.delay(16) // ~60 FPS
        }
    }

    // Mode / Tool
    var mode by remember { mutableStateOf(Mode.Move) }
    var activeTool by remember { mutableStateOf(ToolKind.SetSquare45) } // default to triangle to see it immediately
    
    // Smooth pan state for better performance
    var smoothPan by remember { mutableStateOf(Vec2(0f, 0f)) }
    
    // Apply smooth pan changes to viewport with debouncing
    LaunchedEffect(smoothPan) {
        if (smoothPan != Vec2(0f, 0f)) {
            kotlinx.coroutines.delay(16) // Debounce to ~60fps
            vm.update { s ->
                s.copy(
                    viewport = s.viewport.copy(
                        pan = s.viewport.pan + smoothPan
                    )
                )
            }
            smoothPan = Vec2(0f, 0f) // Reset after applying
        }
    }

    // Tool transform (shared: ruler or set square)
    var toolPos by remember { mutableStateOf(Vec2(0f, 0f)) }
    var toolAngleRad by remember { mutableStateOf(0f) }
    var compassRadius by remember { mutableStateOf(60f) }
    var protractorBaseAngle by remember { mutableStateOf<Float?>(null) }
    var compassMode by remember { mutableStateOf(CompassMode.Circle) }
    var arcStartAngle by remember { mutableStateOf<Float?>(null) }
    var arcEndAngle by remember { mutableStateOf<Float?>(null) }

    // Drag session
    var dragging by remember { mutableStateOf(false) }
    var startW by remember { mutableStateOf(Vec2(0f, 0f)) }
    var endW by remember { mutableStateOf<Vec2?>(null) }
    var hud by remember { mutableStateOf("") }
    var lastSnap by remember { mutableStateOf("") }
    var highlight by remember { mutableStateOf<Vec2?>(null) }
    var snapOff by remember { mutableStateOf(false) }
    var canvasSize by remember { mutableStateOf(IntSize(0, 0)) }

    // Permission launcher for Android 13+
    val ctx = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val width = if (canvasSize.width > 0) canvasSize.width else 1080
            val height = if (canvasSize.height > 0) canvasSize.height else 1920
            saveToGallery(ctx, state, width, height)
        } else {
            Toast.makeText(ctx, "Permission needed to save to Gallery", Toast.LENGTH_SHORT).show()
        }
    }

    // Center newly selected Protractor in the current viewport if not placed yet
    LaunchedEffect(activeTool, canvasSize, state.viewport.pan, state.viewport.zoom) {
        if (activeTool == ToolKind.Protractor && toolPos.x == 0f && toolPos.y == 0f && canvasSize.width > 0 && canvasSize.height > 0) {
            val cx = state.viewport.pan.x + (canvasSize.width / state.viewport.zoom) / 2f
            val cy = state.viewport.pan.y + (canvasSize.height / state.viewport.zoom) / 2f
            toolPos = Vec2(cx, cy)
        }
    }

    // If never positioned, auto-center any tool once when canvas is known
    LaunchedEffect(canvasSize) {
        if (toolPos.x == 0f && toolPos.y == 0f && canvasSize.width > 0 && canvasSize.height > 0) {
            val cx = state.viewport.pan.x + (canvasSize.width / state.viewport.zoom) / 2f
            val cy = state.viewport.pan.y + (canvasSize.height / state.viewport.zoom) / 2f
            toolPos = Vec2(cx, cy)
        }
    }

    // Points index (endpoints + midpoints + intersections + circle centers + arc endpoints)
    val lines = remember(state.shapes) { state.shapes.filterIsInstance<Shape.Line>() }
    val circles = remember(state.shapes) { state.shapes.filterIsInstance<Shape.Circle>() }
    val arcs = remember(state.shapes) { state.shapes.filterIsInstance<Shape.Arc>() }
    val pointIndex = remember(lines, circles, arcs) {
        // Finer cell size improves endpoint search accuracy
        SpatialIndex(32f).apply {
            val pts = collectLinePoints(lines) +
                     collectIntersections(lines) +
                     collectCircleCenters(circles) +
                     collectArcPoints(arcs) +
                     collectCircleLineIntersections(circles, lines) +
                     collectCircleIntersections(circles)
            insertAll(pts)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Snappy Ruler Set") })
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { vm.undo() }) { 
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo") 
                        }
                        IconButton(onClick = { vm.redo() }) { 
                            Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo") 
                        }
                        IconButton(onClick = { vm.clear() }) { 
                            Icon(Icons.Filled.Clear, contentDescription = "Clear Screen") 
                        }
                        IconButton(onClick = {
                            val width = if (canvasSize.width > 0) canvasSize.width else 1080
                            val height = if (canvasSize.height > 0) canvasSize.height else 1920
                            val shareIntent = com.rkt.snappyrulerset.export.Exporter.createShareIntentPng(context, state, state.viewport, width, height)
                            context.startActivity(Intent.createChooser(shareIntent, "Share Drawing"))
                        }) { 
                            Icon(Icons.Filled.Share, contentDescription = "Share") 
                        }
                        IconButton(onClick = {
                            val width = if (canvasSize.width > 0) canvasSize.width else 1080
                            val height = if (canvasSize.height > 0) canvasSize.height else 1920

                            // Check and request permissions for Android 13+
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val permission = Manifest.permission.READ_MEDIA_IMAGES
                                if (ContextCompat.checkSelfPermission(ctx, permission) != PackageManager.PERMISSION_GRANTED) {
                                    permissionLauncher.launch(permission)
                                    return@IconButton
                                }
                            }

                            saveToGallery(ctx, state, width, height)
                        }) { 
                            Icon(Icons.Filled.Save, contentDescription = "Save") 
                        }
                    }
                }
            )
        }
    ) { pad ->
        Box(Modifier
            .fillMaxSize()
            .padding(pad)) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F8F8))
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(
                        state.viewport,
                        state.snapping,
                        mode,
                        activeTool,
                        pointIndex,
                        toolPos,
                        toolAngleRad
                    ) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            var transformMode = false

                            if (mode == Mode.Line || mode == Mode.RulerLine || mode == Mode.SquareLine || mode == Mode.Protractor || mode == Mode.Compass || mode == Mode.Pencil) {
                                // Long-press = snap temporarily off
                                snapOff = withTimeoutOrNull(350) {
                                    while (true) {
                                        val e = awaitPointerEvent()
                                        if (e.changes.any { it.positionChanged() }) break
                                        if (e.changes.all { !it.pressed }) break
                                    }
                                } == null
                            }

                            do {
                                val event = awaitPointerEvent()
                                val changes = event.changes
                                val fingers = changes.count { it.pressed }
                                if (fingers >= 2) transformMode = true

                                if (transformMode) {
                                    val panDelta = event.calculatePan()
                                    val zoomDelta = event.calculateZoom()
                                    val rotDelta = event.calculateRotation()

                                    if (mode == Mode.Tool) {
                                        // Move/rotate ACTIVE TOOL (ruler or set-square)
                                        toolPos += Vec2(
                                            panDelta.x,
                                            panDelta.y
                                        ) * (1f / state.viewport.zoom)
                                        var a = toolAngleRad + rotDelta
                                        val snap = snapAngleIfClose(a, 4f) // snap to 0/30/45/60/90
                                        if (snap.snapped) {
                                            if (lastSnap != "toolAngle") {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                lastSnap = "toolAngle"
                                            }
                                            a = snap.value
                                        }
                                        toolAngleRad = a
                                        changes.forEach { it.consume() }
                                    } else {
                                        // Pan/zoom canvas in other modes
                                        vm.update { s ->
                                            s.copy(
                                                viewport = s.viewport.copy(
                                                    pan = s.viewport.pan + Vec2(
                                                        panDelta.x,
                                                        panDelta.y
                                                    ) * (1f / s.viewport.zoom),
                                                    zoom = (s.viewport.zoom * zoomDelta).coerceIn(
                                                        0.25f,
                                                        6f
                                                    )
                                                )
                                            )
                                        }
                                        changes.forEach { it.consume() }
                                        if (dragging) {
                                            dragging = false; endW = null
                                        }
                                    }
                                } else {
                                    when (mode) {
                                        Mode.Move -> {
                                            val panDelta = event.calculatePan()
                                            // Use smooth pan for better performance
                                            smoothPan += Vec2(panDelta.x, panDelta.y) * (1f / state.viewport.zoom)
                                            changes.forEach { it.consume() }
                                        }

                                        Mode.Line, Mode.RulerLine, Mode.SquareLine, Mode.Protractor, Mode.Compass, Mode.Pencil -> {
                                            val pos = changes.firstOrNull()?.position ?: continue
                                            val world = screenToWorld(pos, state.viewport)

                                            if (!dragging) {
                                                startW = world
                                                endW = null
                                                dragging = true
                                                if (mode == Mode.Protractor) {
                                                    // Reset protractor for new measurement
                                                    protractorBaseAngle = null
                                                    hud = "Click to set first ray"
                                                }
                                                if (mode == Mode.Pencil) {
                                                    vm.update { s -> s.copy(shapes = s.shapes + Shape.Path(points = listOf(world))) }
                                                }
                                            } else {
                                                // pick direction
                                                val dir = when (mode) {
                                                    Mode.Line -> {
                                                        val raw = atan2(
                                                            (world - startW).y,
                                                            (world - startW).x
                                                        )
                                                        val ang =
                                                            if (state.snapping && !snapOff) nearestCommonAngle(
                                                                raw
                                                            ) else raw
                                                        fromAngle(ang)
                                                    }

                                                    Mode.RulerLine -> fromAngle(toolAngleRad)
                                                    Mode.SquareLine -> {
                                                        val dirs = triangleEdgeDirs(
                                                            activeTool,
                                                            toolPos,
                                                            toolAngleRad,
                                                            220f
                                                        )
                                                        val v = norm(world - startW)
                                                        dirs.maxBy { abs(dot(v, it)) }
                                                    }

                                                    Mode.Protractor -> fromAngle(0f)
                                                    Mode.Compass -> fromAngle(0f)
                                                    Mode.Pencil -> fromAngle(0f)

                                                    else -> fromAngle(0f)
                                                }

                                                var projected = when (mode) {
                                                    Mode.Compass, Mode.Pencil -> world
                                                    else -> projectOntoLine(startW, world, dir)
                                                }
                                                highlight = null

                                                // Enhanced snapping with priority system
                                                if (state.snapping && !snapOff) {
                                                    val radiusPx = dynamicSnapRadiusPx(state.viewport.zoom, basePx = 24f)
                                                    val snapCandidates = mutableListOf<SnapCandidate>()

                                                    // 1. Point snaps (highest priority) - more generous for endpoints
                                                    val near = pointIndex.queryNear(projected)
                                                    near.forEach { point ->
                                                        val d = distance(point, projected) * state.viewport.zoom
                                                        val effectiveRadius = if (lastSnap == "point") radiusPx * 1.5f else radiusPx
                                                        if (d <= effectiveRadius) {
                                                            snapCandidates.add(SnapCandidate(point, 1, d, "point"))
                                                        }
                                                    }

                                                    // 2. Segment snaps (medium priority)
                                                    if (mode != Mode.Compass) {
                                                        lines.forEach { seg ->
                                                            val cp = closestPointOnSegment(projected, seg.a, seg.b)
                                                            val d = distance(projected, cp) * state.viewport.zoom
                                                            if (d <= radiusPx) {
                                                                snapCandidates.add(SnapCandidate(cp, 2, d, "segment"))
                                                            }
                                                        }
                                                    }

                                                    // 3. Grid snaps (lowest priority)
                                                    val gridP = snapToGrid(projected, gridMm, dpi)
                                                    val gridD = distance(gridP, projected) * state.viewport.zoom
                                                    if (gridD <= radiusPx) {
                                                        snapCandidates.add(SnapCandidate(gridP, 3, gridD, "grid"))
                                                    }

                                                    // Pick best candidate (closest with highest priority)
                                                    val best = snapCandidates.minByOrNull { it.priority * 1000f + it.distancePx }
                                                    if (best != null) {
                                                        projected = best.pos
                                                        highlight = best.pos
                                                        if (lastSnap != best.label) {
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            lastSnap = best.label
                                                        }
                                                    }
                                                }

                                                when (mode) {
                                                    Mode.Protractor -> {
                                                        // First click sets base angle, second click measures from base
                                                        if (protractorBaseAngle == null) {
                                                            protractorBaseAngle = atan2((projected - toolPos).y, (projected - toolPos).x)
                                                            hud = "Set first ray"
                                                        } else {
                                                            val base = protractorBaseAngle!!
                                                            val cur = atan2((projected - toolPos).y, (projected - toolPos).x)
                                                            var delta = degrees(cur - base)
                                                            while (delta < 0f) delta += 360f
                                                            if (delta > 180f) delta = 360f - delta

                                                            // snap readout to 0.5° accuracy and hard snap to common angles
                                                            val snappedHard = snapAngleIfClose(cur, 2f) // tighter threshold
                                                            val shown = if (snappedHard.snapped) degrees(snappedHard.value - base).absoluteValue else delta
                                                            if (snappedHard.snapped && lastSnap != "angleHard") {
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); lastSnap = "angleHard"
                                                            }
                                                            // Round to nearest 0.5° for display
                                                            val rounded = (shown * 2f).roundToInt() / 2f
                                                            hud = String.format(Locale.US,"%.1f°", rounded)
                                                        }
                                                        endW = projected
                                                    }
                                                    Mode.Compass -> {
                                                        val r = distance(startW, projected)
                                                        compassRadius = r
                                                        val mm = r * mmPerPx
                                                        val cm = mm / 10f

                                                        if (compassMode == CompassMode.Arc) {
                                                            if (arcStartAngle == null) {
                                                                arcStartAngle = atan2((projected - startW).y, (projected - startW).x)
                                                                hud = "Set arc start angle"
                                                            } else {
                                                                arcEndAngle = atan2((projected - startW).y, (projected - startW).x)
                                                                val sweep = arcEndAngle!! - arcStartAngle!!
                                                                val sweepDeg = degrees(sweep)
                                                                hud = String.format(Locale.US, "r=%.1f cm | %.1f° arc", cm, sweepDeg)
                                                            }
                                                        } else {
                                                            hud = String.format(Locale.US, "r=%.1f cm (%.1f mm)", cm, mm)
                                                        }
                                                        endW = projected
                                                    }
                                                    Mode.Pencil -> {
                                                        endW = projected
                                                        vm.update { s ->
                                                            val last = s.shapes.lastOrNull()
                                                            if (last is Shape.Path) {
                                                                val newPts = last.points + projected
                                                                s.copy(shapes = s.shapes.dropLast(1) + last.copy(points = newPts))
                                                            } else s
                                                        }
                                                    }
                                                    else -> {
                                                        endW = projected
                                                        val mm = distance(startW, projected) * mmPerPx
                                                        val cm = mm / 10f
                                                        val deg = degrees(
                                                            atan2(
                                                                dir.y,
                                                                dir.x
                                                            )
                                                        ).let { if (it < 0) it + 360f else it }
                                                        hud = String.format(Locale.US,"%.1f cm (%.1f mm) | %.1f°", cm, mm, deg)
                                                    }
                                                }
                                            }
                                        }

                                        Mode.Tool -> { /* single-finger noop while editing tool */
                                        }
                                    }
                                }
                            } while (event.changes.any { it.pressed })

                            if ((mode == Mode.Line || mode == Mode.RulerLine || mode == Mode.SquareLine || mode == Mode.Compass || mode == Mode.Protractor || mode == Mode.Pencil) && !transformMode && dragging) {
                                dragging = false
                                when (mode) {
                                    Mode.Compass -> {
                                        endW?.let { e ->
                                            val r = distance(startW, e)
                                            if (compassMode == CompassMode.Arc && arcStartAngle != null && arcEndAngle != null) {
                                                val sweep = arcEndAngle!! - arcStartAngle!!
                                                vm.update { s -> s.copy(shapes = s.shapes + Shape.Arc(startW, r, arcStartAngle!!, sweep)) }
                                                arcStartAngle = null
                                                arcEndAngle = null
                                            } else {
                                                vm.update { s -> s.copy(shapes = s.shapes + Shape.Circle(startW, r)) }
                                            }
                                        }
                                    }
                                    Mode.Protractor -> {
                                        // measurement only, no shape
                                    }
                                    Mode.Pencil -> {
                                        // already added during drag
                                    }
                                    else -> {
                                        endW?.let { e ->
                                            vm.update { s ->
                                                s.copy(
                                                    shapes = s.shapes + Shape.Line(
                                                        startW,
                                                        e
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                                endW = null
                            }
                            snapOff = false
                        }
                    }
            ) {
                // world->screen with smooth pan
                fun w2s(p: Vec2): Offset {
                    val z = state.viewport.zoom
                    val pan = if (mode == Mode.Move) {
                        state.viewport.pan + smoothPan
                    } else {
                        state.viewport.pan
                    }
                    return Offset((p.x - pan.x) * z, (p.y - pan.y) * z)
                }

                // Grid - optimized for performance
                val stepWorld = mmToPx(gridMm, dpi)
                val step = stepWorld * state.viewport.zoom
                if (step > 8f && step < 200f) { // Only draw grid when visible and not too dense
                    val w = size.width
                    val h = size.height
                    val currentPan = if (mode == Mode.Move) {
                        state.viewport.pan + smoothPan
                    } else {
                        state.viewport.pan
                    }
                    val startX = -((currentPan.x * state.viewport.zoom) % step)
                    val startY = -((currentPan.y * state.viewport.zoom) % step)

                    // Limit grid lines for performance
                    val maxLines = 50
                    val xCount = minOf(maxLines, (w / step).toInt() + 2)
                    val yCount = minOf(maxLines, (h / step).toInt() + 2)

                    for (i in 0 until xCount) {
                        val x = startX + i * step
                        if (x >= 0 && x <= w) {
                            drawLine(Color(0xFFE0E0E0), Offset(x, 0f), Offset(x, h), 1f)
                        }
                    }
                    for (i in 0 until yCount) {
                        val y = startY + i * step
                        if (y >= 0 && y <= h) {
                            drawLine(Color(0xFFE0E0E0), Offset(0f, y), Offset(w, y), 1f)
                        }
                    }
                }

                // Existing shapes
                state.shapes.forEach { s ->
                    when (s) {
                        is Shape.Line -> drawLine(Color.Black, w2s(s.a), w2s(s.b), 3f)
                        is Shape.Circle -> {
                            val center = w2s(s.center)
                            val radius = s.r * state.viewport.zoom
                            drawCircle(Color.Black, radius, center, style = Stroke(width = 3f))
                        }
                        is Shape.Arc -> {
                            val center = w2s(s.center)
                            val radius = s.r * state.viewport.zoom
                            drawArc(
                                color = Color.Black,
                                startAngle = degrees(s.startRad),
                                sweepAngle = degrees(s.sweepRad),
                                useCenter = false,
                                topLeft = center - Offset(radius, radius),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = 3f)
                            )
                        }
                        is Shape.Path -> {
                            val pts = s.points.map { w2s(it) }
                            for (i in 1 until pts.size) {
                                drawLine(Color.Black, pts[i-1], pts[i], 3f)
                            }
                        }
                        else -> {}
                    }
                }

                // --- Tool overlays ---
                when (activeTool) {
                    ToolKind.Ruler -> if (mode == Mode.Tool || mode == Mode.RulerLine) {
                        // Ruler: long blue line + body
                        val z = state.viewport.zoom
                        val center = w2s(toolPos)
                        val dir = fromAngle(toolAngleRad)
                        val ortho = Vec2(-dir.y, dir.x)
                        val halfLen = 1000f * z
                        val halfThick = 12f
                        val a = center + Offset(dir.x * halfLen, dir.y * halfLen)
                        val b = center - Offset(dir.x * halfLen, dir.y * halfLen)
                        drawLine(Color(0xFF3949AB), a, b, 3f)
                        val a1 = a + Offset(ortho.x * halfThick, ortho.y * halfThick)
                        val b1 = b + Offset(ortho.x * halfThick, ortho.y * halfThick)
                        val a2 = a - Offset(ortho.x * halfThick, ortho.y * halfThick)
                        val b2 = b - Offset(ortho.x * halfThick, ortho.y * halfThick)
                        drawLine(Color(0x303949AB), a1, b1, 8f)
                        drawLine(Color(0x303949AB), a2, b2, 8f)
                    }

                    ToolKind.SetSquare45, ToolKind.SetSquare30_60 -> if (mode == Mode.Tool || mode == Mode.SquareLine) {
                        // Triangle outline (edges used for SquareLine)
                        val c = toolPos
                        val size = 220f // world units width (tweak)
                        val pts = triangleWorldPoints(activeTool, c, toolAngleRad, size)
                        fun toOff(v: Vec2) = w2s(v)
                        drawLine(Color(0xFF00695C), toOff(pts[0]), toOff(pts[1]), 3f)
                        drawLine(Color(0xFF00695C), toOff(pts[1]), toOff(pts[2]), 3f)
                        drawLine(Color(0xFF00695C), toOff(pts[2]), toOff(pts[0]), 3f)
                    }

                    ToolKind.Protractor -> if (mode == Mode.Tool || mode == Mode.Protractor) {
                        val center = w2s(toolPos)
                        val r = 140f * state.viewport.zoom
                        // base line showing zero angle
                        val dir = fromAngle(toolAngleRad)
                        drawCircle(Color(0x803498DB), r, center, style = Stroke(width = 2f))
                        drawLine(Color(0xFF1E88E5), center, center + Offset(dir.x * r, dir.y * r), 3f)
                        // small tick when snapping angle (visual hint)
                        if (lastSnap == "angleHard" && endW != null) {
                            val p = w2s(endW!!)
                            drawCircle(Color(0xFF1E88E5), 5f, p)
                        }
                    }

                    ToolKind.Compass -> if (mode == Mode.Tool || mode == Mode.Compass) {
                        val center = w2s(toolPos)
                        val r = compassRadius * state.viewport.zoom
                        drawCircle(Color(0x80388E3C), r, center, style = Stroke(width = 2f))
                        // draw leg towards current pointer if dragging
                        if (mode == Mode.Compass && endW != null) {
                            drawLine(Color(0xFF388E3C), center, w2s(endW!!), 3f)
                        }
                    }
                }

                // Temp line
                if (dragging && endW != null && (mode == Mode.Line || mode == Mode.RulerLine || mode == Mode.SquareLine)) {
                    drawLine(Color(0xFF1565C0), w2s(startW), w2s(endW!!), 4f)
                }
                if (dragging && endW != null && mode == Mode.Protractor) {
                    // show two rays from toolPos: base and current
                    val r = 180f * state.viewport.zoom
                    val a = w2s(toolPos)

                    // Draw base ray (first click)
                    if (protractorBaseAngle != null) {
                        val b1 = a + Offset(cos(protractorBaseAngle!!) * r, sin(protractorBaseAngle!!) * r)
                        drawLine(Color(0xFF1E88E5), a, b1, 4f)
                    }

                    // Draw current ray (second click)
                    val currentAngle = atan2((endW!! - toolPos).y, (endW!! - toolPos).x)
                    val b2 = a + Offset(cos(currentAngle) * r, sin(currentAngle) * r)
                    drawLine(Color(0xFF1E88E5), a, b2, 4f)
                }
                if (dragging && endW != null && mode == Mode.Compass) {
                    val center = w2s(startW)
                    val radius = distance(startW, endW!!) * state.viewport.zoom

                    if (compassMode == CompassMode.Arc && arcStartAngle != null) {
                        // preview arc
                        val startAngle = arcStartAngle!!
                        val endAngle = atan2((endW!! - startW).y, (endW!! - startW).x)
                        val sweepAngle = endAngle - startAngle

                        drawArc(
                            color = Color(0xFF388E3C),
                            startAngle = degrees(startAngle),
                            sweepAngle = degrees(sweepAngle),
                            useCenter = false,
                            topLeft = center - Offset(radius, radius),
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                            style = Stroke(width = 2f)
                        )
                        // draw radius lines
                        val startDir = fromAngle(startAngle)
                        val endDir = fromAngle(endAngle)
                        drawLine(Color(0xFF388E3C), center, center + Offset(startDir.x * radius, startDir.y * radius), 3f)
                        drawLine(Color(0xFF388E3C), center, center + Offset(endDir.x * radius, endDir.y * radius), 3f)
                    } else {
                        // preview circle
                        drawCircle(Color(0xFF388E3C), radius, center, style = Stroke(width = 2f))
                        // draw radius line
                        drawLine(Color(0xFF388E3C), center, w2s(endW!!), 3f)
                    }
                }

                // Snap highlight with visual affordance - larger and more attractive
                highlight?.let { hp ->
                    val center = w2s(hp)
                    val baseRadius = 12f

                    // Different colors for different snap types
                    val color = when (lastSnap) {
                        "point" -> Color(0xFF2E7D32) // Green for points
                        "segment" -> Color(0xFF1976D2) // Blue for segments
                        "grid" -> Color(0xFF757575) // Gray for grid
                        else -> Color(0xFF2E7D32)
                    }

                    // Outer glow ring
                    drawCircle(color.copy(alpha = 0.3f), baseRadius + 6f, center, style = Stroke(width = 3f))
                    // Main ring
                    drawCircle(color, baseRadius + 2f, center, style = Stroke(width = 4f))
                    // Inner circle
                    drawCircle(color, baseRadius, center, style = Stroke(width = 2f))
                    // Center dot
                    drawCircle(color, 5f, center)
                }
            }

            // Drawing options in top left corner with glassy effect
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 56.dp, start = 12.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.White.copy(alpha = 0.15f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.4f),
                                Color.White.copy(alpha = 0.2f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Drawing modes with circular icons (scrollable)
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Move/Drag tool
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularIconButton(
                            onClick = { mode = Mode.Move },
                            selected = mode == Mode.Move,
                            icon = Icons.Default.PanTool,
                            contentDescription = "Move/Drag Tool"
                        )
                        Text(
                            text = "Move",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (mode == Mode.Move) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.9f)
                        )
                    }
                    
                    // Pencil tool
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularIconButton(
                            onClick = { mode = Mode.Pencil },
                            selected = mode == Mode.Pencil,
                            icon = Icons.Default.Edit,
                            contentDescription = "Pencil Tool"
                        )
                        Text(
                            text = "Pencil",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (mode == Mode.Pencil) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.9f)
                        )
                    }
                    
                    // Line tool
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularIconButton(
                            onClick = { mode = Mode.Line },
                            selected = mode == Mode.Line,
                            icon = Icons.Default.ShowChart,
                            contentDescription = "Line Tool"
                        )
                        Text(
                            text = "Line",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (mode == Mode.Line) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.9f)
                        )
                    }
                    
                    // Ruler line tool
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularIconButton(
                            onClick = { mode = Mode.RulerLine },
                            selected = mode == Mode.RulerLine,
                            icon = Icons.Default.Straighten,
                            contentDescription = "Ruler Line Tool"
                        )
                        Text(
                            text = "Ruler",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (mode == Mode.RulerLine) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.9f)
                        )
                    }
                    
                    // Square line tool
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularIconButton(
                            onClick = { mode = Mode.SquareLine },
                            selected = mode == Mode.SquareLine,
                            icon = Icons.Default.CropSquare,
                            contentDescription = "Square Line Tool"
                        )
                        Text(
                            text = "Square",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (mode == Mode.SquareLine) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.9f)
                        )
                    }
                    
                    // Protractor tool
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularIconButton(
                            onClick = { mode = Mode.Protractor },
                            selected = mode == Mode.Protractor,
                            icon = Icons.Default.Explore,
                            contentDescription = "Protractor Tool"
                        )
                        Text(
                            text = "Protractor",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (mode == Mode.Protractor) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.9f)
                        )
                    }
                    
                    // Compass tool
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularIconButton(
                            onClick = { mode = Mode.Compass },
                            selected = mode == Mode.Compass,
                            icon = Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Compass Tool"
                        )
                        Text(
                            text = "Compass",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (mode == Mode.Compass) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            if (hud.isNotEmpty() && (mode == Mode.Line || mode == Mode.RulerLine || mode == Mode.SquareLine || mode == Mode.Protractor || mode == Mode.Compass)) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(Color(0xAA000000))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        hud,
                        color = Color.White
                    )
                    if (mode == Mode.Line || mode == Mode.RulerLine || mode == Mode.SquareLine || mode == Mode.Compass) {
                        Text(
                            "Calibrated for ${dpi.toInt()}dpi (${String.format("%.2f", mmPerPx)}mm/px)",
                            color = Color(0xFFCCCCCC),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "FPS: ${String.format("%.1f", performanceMonitor.getCurrentFps())}",
                            color = Color(0xFFCCCCCC),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

/* ---------------- helpers ---------------- */

private fun saveToGallery(ctx: Context, state: com.rkt.snappyrulerset.model.DrawingState, width: Int, height: Int) {
    try {
        // Save to gallery using MediaStore (Android 10+)
        val contentResolver = ctx.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "snappy_ruler_${System.currentTimeMillis()}.png")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SnappyRulerSet")
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let { imageUri ->
            contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                val bitmap = com.rkt.snappyrulerset.export.Exporter.createBitmap(state, state.viewport, width, height)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                Toast.makeText(ctx, "Saved to Gallery", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            // Fallback to cache directory if MediaStore fails
            val file = File(ctx.cacheDir, "snappy_export.png")
            com.rkt.snappyrulerset.export.Exporter.exportPng(state, state.viewport, width, height, file)
            Toast.makeText(ctx, "Saved to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        // Fallback to cache directory if any error occurs
        val file = File(ctx.cacheDir, "snappy_export.png")
        com.rkt.snappyrulerset.export.Exporter.exportPng(state, state.viewport, width, height, file)
        Toast.makeText(ctx, "Saved to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
    }
}

private fun screenToWorld(p: Offset, vp: Viewport) =
    Vec2(vp.pan.x + p.x / vp.zoom, vp.pan.y + p.y / vp.zoom)

private fun fromAngle(theta: Float) = Vec2(cos(theta), sin(theta))

// Project onto infinite line through 'origin' with direction 'dir'
private fun projectOntoLine(origin: Vec2, p: Vec2, dir: Vec2): Vec2 {
    val v = p - origin
    val t = dot(v, dir)
    return origin + dir * t
}

private data class SnapResult(val value: Float, val snapped: Boolean)

private val COMMON_ANGLES = floatArrayOf(0f, 30f, 45f, 60f, 90f, 120f, 135f, 150f, 180f)

private fun nearestCommonAngle(raw: Float): Float {
    val twoPI = (Math.PI * 2).toFloat()
    fun norm(a: Float): Float {
        var x = (a % twoPI + twoPI) % twoPI
        if (x > Math.PI) x -= twoPI
        return x
    }

    val bestDeg = COMMON_ANGLES.minBy { abs(norm(raw - radians(it))) }
    return radians(bestDeg)
}

private fun snapAngleIfClose(rawRad: Float, thresholdDeg: Float): SnapResult {
    val bestDeg = COMMON_ANGLES.minBy { abs(degrees(rawRad) - it).let { d -> min(d, 360f - d) } }
    val bestRad = radians(bestDeg)
    val delta = abs(degrees(rawRad - bestRad)).let { d -> min(d, 360f - d) }
    return if (delta <= thresholdDeg) SnapResult(bestRad, true) else SnapResult(rawRad, false)
}

private fun mmToPx(mm: Float, dpi: Float) = (dpi / 25.4f) * mm
private fun dynamicSnapRadiusPx(zoom: Float, basePx: Float = 16f) =
    (basePx / zoom).coerceIn(6f, 28f)

private fun snapToGrid(p: Vec2, gridMm: Float, dpi: Float): Vec2 {
    val s = mmToPx(gridMm, dpi)
    fun r(v: Float) = round(v / s) * s
    return Vec2(r(p.x), r(p.y))
}

// ---- Set Square geometry ----
private fun triangleEdgeDirs(
    kind: ToolKind,
    center: Vec2,
    baseAngle: Float,
    size: Float
): List<Vec2> {
    val pts = triangleWorldPoints(kind, center, baseAngle, size)
    if (pts.size < 3) return listOf(fromAngle(baseAngle)) // fallback

    return listOf(
        norm(pts[1] - pts[0]),
        norm(pts[2] - pts[1]),
        norm(pts[0] - pts[2])
    )
}

private fun triangleWorldPoints(
    kind: ToolKind,
    center: Vec2,
    baseAngle: Float,
    size: Float
): List<Vec2> {
    // Build an axis-aligned triangle then rotate by baseAngle and translate to center
    val raw = when (kind) {
        ToolKind.SetSquare45 -> {
            val a = Vec2(-size / 2f, -size / 2f)
            val b = Vec2(size / 2f, -size / 2f)
            val c = Vec2(-size / 2f, size / 2f)
            listOf(a, b, c)
        }

        ToolKind.SetSquare30_60 -> {
            val short = size
            val long = size * sqrt(3f)
            val a = Vec2(-short / 2f, -long / 2f) // vertical long leg
            val b = Vec2(short / 2f, -long / 2f)
            val c = Vec2(-short / 2f, long / 2f)
            listOf(a, b, c)
        }

        else -> listOf(Vec2(0f, 0f), Vec2(0f, 0f), Vec2(0f, 0f))
    }
    val ca = cos(baseAngle);
    val sa = sin(baseAngle)
    fun rot(p: Vec2) = Vec2(p.x * ca - p.y * sa, p.x * sa + p.y * ca)
    return raw.map { center + rot(it) }
}


@Composable
fun CircularIconButton(
    onClick: () -> Unit,
    selected: Boolean,
    icon: ImageVector,
    contentDescription: String
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    } else {
        Color.White.copy(alpha = 0.8f)
    }
    
    val iconColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Black.copy(alpha = 0.8f)
    }
    
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Black.copy(alpha = 0.3f)
    }
    
    val shadowColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    } else {
        Color.Black.copy(alpha = 0.2f)
    }
    
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(52.dp)
            .background(
                color = backgroundColor,
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = borderColor,
                shape = CircleShape
            )
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SnappyRulerSetTheme {
        SnappyRulerSetTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                DrawingScreen()
            }
        }
    }
}
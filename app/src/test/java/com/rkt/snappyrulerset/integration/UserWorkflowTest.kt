package com.rkt.snappyrulerset.integration

import com.rkt.snappyrulerset.domain.entity.*
import com.rkt.snappyrulerset.domain.usecase.SnappingEngine
import com.rkt.snappyrulerset.domain.usecase.SpatialGrid
import com.rkt.snappyrulerset.domain.usecase.collectLinePoints
import com.rkt.snappyrulerset.presentation.viewmodel.DrawingViewModel
import com.rkt.snappyrulerset.TestUtils
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

/**
 * Integration tests that simulate real user workflows
 */
class UserWorkflowTest {

    @Test
    fun `user can draw a simple line and undo it`() = runTest {
        val viewModel = DrawingViewModel()
        val line = TestUtils.createTestLine(0f, 0f, 100f, 100f)
        
        // User draws a line
        viewModel.update { state ->
            state.copy(shapes = listOf(line))
        }
        
        // Verify line was added
        assertEquals("Should have one shape", 1, viewModel.state.value.shapes.size)
        assertTrue("Should contain the line", viewModel.state.value.shapes.contains(line))
        
        // User undoes the action
        viewModel.undo()
        
        // Verify line was removed
        assertTrue("Should have no shapes after undo", viewModel.state.value.shapes.isEmpty())
    }

    @Test
    fun `user can draw multiple shapes and clear all`() = runTest {
        val viewModel = DrawingViewModel()
        val line = TestUtils.createTestLine(0f, 0f, 100f, 100f)
        val circle = TestUtils.createTestCircle(50f, 50f, 25f)
        val point = TestUtils.createTestPoint(25f, 25f)
        
        // User draws multiple shapes
        viewModel.update { state ->
            state.copy(shapes = listOf(line, circle, point))
        }
        
        // Verify all shapes were added
        assertEquals("Should have three shapes", 3, viewModel.state.value.shapes.size)
        
        // User clears all
        viewModel.clear()
        
        // Verify all shapes were removed
        assertTrue("Should have no shapes after clear", viewModel.state.value.shapes.isEmpty())
    }

    @Test
    fun `user can draw, undo, and redo multiple times`() = runTest {
        val viewModel = DrawingViewModel()
        val shapes = listOf(
            TestUtils.createTestLine(0f, 0f, 50f, 50f),
            TestUtils.createTestCircle(25f, 25f, 10f),
            TestUtils.createTestPoint(10f, 10f)
        )
        
        // Add shapes one by one
        shapes.forEach { shape ->
            viewModel.update { state ->
                state.copy(shapes = state.shapes + shape)
            }
        }
        
        // Verify all shapes are present
        assertEquals("Should have all shapes", shapes.size, viewModel.state.value.shapes.size)
        
        // Undo all actions
        repeat(shapes.size) {
            viewModel.undo()
        }
        
        // Verify all shapes are gone
        assertTrue("Should have no shapes after undoing all", viewModel.state.value.shapes.isEmpty())
        
        // Redo all actions
        repeat(shapes.size) {
            viewModel.redo()
        }
        
        // Verify all shapes are back
        assertEquals("Should have all shapes after redoing", shapes.size, viewModel.state.value.shapes.size)
        shapes.forEach { shape ->
            assertTrue("Should contain shape $shape", viewModel.state.value.shapes.contains(shape))
        }
    }

    @Test
    fun `snapping system works with real drawing workflow`() {
        val snappingEngine = SnappingEngine()
        val spatialGrid = SpatialGrid(64f)
        
        // User draws some lines
        val lines = listOf(
            TestUtils.createTestLine(0f, 0f, 100f, 0f),
            TestUtils.createTestLine(0f, 0f, 0f, 100f),
            TestUtils.createTestLine(100f, 0f, 100f, 100f)
        )
        
        // Collect snap points from existing lines
        val snapPoints = collectLinePoints(lines)
        spatialGrid.insertAll(snapPoints)
        
        // User tries to draw a new line near existing endpoints
        val newLineStart = Vec2(0f, 0f) // Should snap to existing endpoint
        val newLineEnd = Vec2(50f, 50f)
        
        // Check if snapping occurs
        val nearbyPoints = spatialGrid.queryNear(newLineStart, 10)
        assertTrue("Should find nearby snap points", nearbyPoints.isNotEmpty())
        
        // Test grid snapping
        val gridSnap = snappingEngine.snapToGrid(newLineEnd, 5f, 160f, 1f)
        assertTrue("Should snap to grid", gridSnap.distancePx < 20f)
    }

    @Test
    fun `angle snapping works with real drawing workflow`() {
        val snappingEngine = SnappingEngine()
        
        // User draws a line at approximately 45 degrees
        val from = Vec2(0f, 0f)
        val to = Vec2(1f, 1.1f) // Slightly off 45 degrees
        
        val (currentAngle, snappedAngle) = snappingEngine.snapAngle(from, to)
        
        // Should snap to exactly 45 degrees
        assertEquals("Should snap to 45 degrees", kotlin.math.PI.toFloat() / 4f, snappedAngle, 0.001f)
    }

    @Test
    fun `viewport operations work correctly`() = runTest {
        val viewModel = DrawingViewModel()
        val initialViewport = viewModel.state.value.viewport
        
        // User pans the viewport
        viewModel.update { state ->
            state.copy(viewport = state.viewport.copy(pan = Vec2(100f, 50f)))
        }
        
        val pannedViewport = viewModel.state.value.viewport
        assertEquals("Pan X should be updated", 100f, pannedViewport.pan.x, 0.001f)
        assertEquals("Pan Y should be updated", 50f, pannedViewport.pan.y, 0.001f)
        
        // User zooms in
        viewModel.update { state ->
            state.copy(viewport = state.viewport.copy(zoom = 2f))
        }
        
        val zoomedViewport = viewModel.state.value.viewport
        assertEquals("Zoom should be updated", 2f, zoomedViewport.zoom, 0.001f)
        
        // User can undo viewport changes
        viewModel.undo()
        val undoneViewport = viewModel.state.value.viewport
        assertEquals("Should undo zoom", 1f, undoneViewport.zoom, 0.001f)
        
        viewModel.undo()
        val finalViewport = viewModel.state.value.viewport
        assertEquals("Should undo pan", initialViewport.pan.x, finalViewport.pan.x, 0.001f)
        assertEquals("Should undo pan", initialViewport.pan.y, finalViewport.pan.y, 0.001f)
    }

    @Test
    fun `tool switching workflow works correctly`() = runTest {
        val viewModel = DrawingViewModel()
        val initialState = viewModel.state.value.tool
        
        // User switches to compass tool
        viewModel.update { state ->
            state.copy(tool = state.tool.copy(kind = ToolKind.Compass))
        }
        
        val compassState = viewModel.state.value.tool
        assertEquals("Should switch to compass", ToolKind.Compass, compassState.kind)
        
        // User switches to protractor tool
        viewModel.update { state ->
            state.copy(tool = state.tool.copy(kind = ToolKind.Protractor))
        }
        
        val protractorState = viewModel.state.value.tool
        assertEquals("Should switch to protractor", ToolKind.Protractor, protractorState.kind)
        
        // User can undo tool changes
        viewModel.undo()
        val undoneState = viewModel.state.value.tool
        assertEquals("Should undo to compass", ToolKind.Compass, undoneState.kind)
        
        viewModel.undo()
        val finalState = viewModel.state.value.tool
        assertEquals("Should undo to initial tool", initialState.kind, finalState.kind)
    }

    @Test
    fun `snapping toggle workflow works correctly`() = runTest {
        val viewModel = DrawingViewModel()
        val initialState = viewModel.state.value.snapping
        
        // User toggles snapping off
        viewModel.update { state ->
            state.copy(snapping = false)
        }
        
        assertFalse("Snapping should be disabled", viewModel.state.value.snapping)
        
        // User toggles snapping back on
        viewModel.update { state ->
            state.copy(snapping = true)
        }
        
        assertTrue("Snapping should be enabled", viewModel.state.value.snapping)
        
        // User can undo snapping changes
        viewModel.undo()
        assertFalse("Should undo to disabled snapping", viewModel.state.value.snapping)
        
        viewModel.undo()
        assertEquals("Should undo to initial snapping state", initialState, viewModel.state.value.snapping)
    }
}

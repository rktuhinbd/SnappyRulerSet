package com.rkt.snappyrulerset.presentation.viewmodel

import com.rkt.snappyrulerset.domain.entity.DrawingState
import com.rkt.snappyrulerset.domain.entity.Shape
import com.rkt.snappyrulerset.domain.entity.Vec2
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

class DrawingViewModelTest {

    @Test
    fun `initial state should be empty`() = runTest {
        val viewModel = DrawingViewModel()
        
        val state = viewModel.state.value
        assertTrue("Initial shapes should be empty", state.shapes.isEmpty())
        assertTrue("Initial snapping should be enabled", state.snapping)
        assertEquals("Initial grid spacing should be 5mm", 5f, state.gridSpacingMm, 0.001f)
    }

    @Test
    fun `update should modify state`() = runTest {
        val viewModel = DrawingViewModel()
        val line = Shape.Line(Vec2(0f, 0f), Vec2(10f, 10f))
        
        viewModel.update { state ->
            state.copy(shapes = listOf(line))
        }
        
        val updatedState = viewModel.state.value
        assertEquals("Should have one shape", 1, updatedState.shapes.size)
        assertEquals("Should contain the line", line, updatedState.shapes.first())
    }

    @Test
    fun `undo should revert to previous state`() = runTest {
        val viewModel = DrawingViewModel()
        val line1 = Shape.Line(Vec2(0f, 0f), Vec2(10f, 10f))
        val line2 = Shape.Line(Vec2(0f, 0f), Vec2(20f, 20f))
        
        // Add first line
        viewModel.update { state ->
            state.copy(shapes = listOf(line1))
        }
        
        // Add second line
        viewModel.update { state ->
            state.copy(shapes = state.shapes + line2)
        }
        
        assertEquals("Should have two shapes", 2, viewModel.state.value.shapes.size)
        
        // Undo
        viewModel.undo()
        
        val undoneState = viewModel.state.value
        assertEquals("Should have one shape after undo", 1, undoneState.shapes.size)
        assertEquals("Should contain first line", line1, undoneState.shapes.first())
    }

    @Test
    fun `redo should restore undone state`() = runTest {
        val viewModel = DrawingViewModel()
        val line1 = Shape.Line(Vec2(0f, 0f), Vec2(10f, 10f))
        val line2 = Shape.Line(Vec2(0f, 0f), Vec2(20f, 20f))
        
        // Add first line
        viewModel.update { state ->
            state.copy(shapes = listOf(line1))
        }
        
        // Add second line
        viewModel.update { state ->
            state.copy(shapes = state.shapes + line2)
        }
        
        // Undo
        viewModel.undo()
        assertEquals("Should have one shape after undo", 1, viewModel.state.value.shapes.size)
        
        // Redo
        viewModel.redo()
        
        val redoneState = viewModel.state.value
        assertEquals("Should have two shapes after redo", 2, redoneState.shapes.size)
        assertTrue("Should contain both lines", redoneState.shapes.contains(line1) && redoneState.shapes.contains(line2))
    }

    @Test
    fun `clear should remove all shapes`() = runTest {
        val viewModel = DrawingViewModel()
        val line1 = Shape.Line(Vec2(0f, 0f), Vec2(10f, 10f))
        val line2 = Shape.Line(Vec2(0f, 0f), Vec2(20f, 20f))
        
        // Add shapes
        viewModel.update { state ->
            state.copy(shapes = listOf(line1, line2))
        }
        
        assertEquals("Should have two shapes", 2, viewModel.state.value.shapes.size)
        
        // Clear
        viewModel.clear()
        
        val clearedState = viewModel.state.value
        assertTrue("Should have no shapes after clear", clearedState.shapes.isEmpty())
    }

    @Test
    fun `undo should not change state if no history`() = runTest {
        val viewModel = DrawingViewModel()
        val initialState = viewModel.state.value
        
        viewModel.undo()
        
        val afterUndoState = viewModel.state.value
        assertEquals("State should not change if no undo available", initialState, afterUndoState)
    }

    @Test
    fun `redo should not change state if no redo available`() = runTest {
        val viewModel = DrawingViewModel()
        val initialState = viewModel.state.value
        
        viewModel.redo()
        
        val afterRedoState = viewModel.state.value
        assertEquals("State should not change if no redo available", initialState, afterRedoState)
    }

    @Test
    fun `multiple updates should maintain history`() = runTest {
        val viewModel = DrawingViewModel()
        val shapes = (0..5).map { i ->
            Shape.Line(Vec2(i.toFloat(), i.toFloat()), Vec2((i + 1).toFloat(), (i + 1).toFloat()))
        }
        
        // Add shapes one by one
        shapes.forEach { shape ->
            viewModel.update { state ->
                state.copy(shapes = state.shapes + shape)
            }
        }
        
        assertEquals("Should have all shapes", shapes.size, viewModel.state.value.shapes.size)
        
        // Undo all
        repeat(shapes.size) {
            viewModel.undo()
        }
        
        val finalState = viewModel.state.value
        assertTrue("Should have no shapes after undoing all", finalState.shapes.isEmpty())
    }

    @Test
    fun `update should save state to history`() = runTest {
        val viewModel = DrawingViewModel()
        val line = Shape.Line(Vec2(0f, 0f), Vec2(10f, 10f))
        
        viewModel.update { state ->
            state.copy(shapes = listOf(line))
        }
        
        // Should be able to undo
        viewModel.undo()
        assertTrue("Should have empty shapes after undo", viewModel.state.value.shapes.isEmpty())
        
        // Should be able to redo
        viewModel.redo()
        assertEquals("Should have one shape after redo", 1, viewModel.state.value.shapes.size)
    }

    @Test
    fun `clear should save state to history`() = runTest {
        val viewModel = DrawingViewModel()
        val line = Shape.Line(Vec2(0f, 0f), Vec2(10f, 10f))
        
        // Add shape
        viewModel.update { state ->
            state.copy(shapes = listOf(line))
        }
        
        // Clear
        viewModel.clear()
        assertTrue("Should have no shapes after clear", viewModel.state.value.shapes.isEmpty())
        
        // Should be able to undo clear
        viewModel.undo()
        assertEquals("Should have one shape after undoing clear", 1, viewModel.state.value.shapes.size)
    }
}

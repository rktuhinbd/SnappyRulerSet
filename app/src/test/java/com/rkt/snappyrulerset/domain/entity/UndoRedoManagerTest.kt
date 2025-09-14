package com.rkt.snappyrulerset.domain.entity

import org.junit.Test
import org.junit.Assert.*

class UndoRedoManagerTest {

    @Test
    fun `push should add state to undo stack`() {
        val manager = UndoRedoManager(5)
        val state1 = DrawingState(shapes = listOf(Shape.Point(Vec2(0f, 0f))))
        val state2 = DrawingState(shapes = listOf(Shape.Point(Vec2(1f, 1f))))
        
        manager.push(state1)
        manager.push(state2)
        
        assertTrue("Should be able to undo", manager.canUndo())
        assertFalse("Should not be able to redo", manager.canRedo())
    }

    @Test
    fun `undo should return previous state`() {
        val manager = UndoRedoManager(5)
        val state1 = DrawingState(shapes = listOf(Shape.Point(Vec2(0f, 0f))))
        val state2 = DrawingState(shapes = listOf(Shape.Point(Vec2(1f, 1f))))
        
        manager.push(state1)
        manager.push(state2)
        
        val undoneState = manager.undo(state2)
        assertEquals("Should return previous state", state1, undoneState)
        assertTrue("Should be able to redo after undo", manager.canRedo())
    }

    @Test
    fun `redo should return next state`() {
        val manager = UndoRedoManager(5)
        val state1 = DrawingState(shapes = listOf(Shape.Point(Vec2(0f, 0f))))
        val state2 = DrawingState(shapes = listOf(Shape.Point(Vec2(1f, 1f))))
        
        manager.push(state1)
        manager.push(state2)
        
        val undoneState = manager.undo(state2)
        val redoneState = manager.redo(undoneState)
        assertEquals("Should return next state", state2, redoneState)
    }

    @Test
    fun `undo should not change state if cannot undo`() {
        val manager = UndoRedoManager(5)
        val state = DrawingState(shapes = listOf(Shape.Point(Vec2(0f, 0f))))
        
        manager.push(state)
        
        val result = manager.undo(state)
        assertEquals("Should return same state if cannot undo", state, result)
    }

    @Test
    fun `redo should not change state if cannot redo`() {
        val manager = UndoRedoManager(5)
        val state = DrawingState(shapes = listOf(Shape.Point(Vec2(0f, 0f))))
        
        manager.push(state)
        
        val result = manager.redo(state)
        assertEquals("Should return same state if cannot redo", state, result)
    }

    @Test
    fun `push should clear redo stack`() {
        val manager = UndoRedoManager(5)
        val state1 = DrawingState(shapes = listOf(Shape.Point(Vec2(0f, 0f))))
        val state2 = DrawingState(shapes = listOf(Shape.Point(Vec2(1f, 1f))))
        val state3 = DrawingState(shapes = listOf(Shape.Point(Vec2(2f, 2f))))
        
        manager.push(state1)
        manager.push(state2)
        manager.undo(state2) // Now we can redo
        assertTrue("Should be able to redo", manager.canRedo())
        
        manager.push(state3) // This should clear redo stack
        assertFalse("Should not be able to redo after new push", manager.canRedo())
    }

    @Test
    fun `should respect max history size`() {
        val manager = UndoRedoManager(3)
        val states = (0..5).map { i ->
            DrawingState(shapes = listOf(Shape.Point(Vec2(i.toFloat(), i.toFloat()))))
        }
        
        states.forEach { manager.push(it) }
        
        // Should be able to undo 2 times (current + 2 previous)
        assertTrue("Should be able to undo", manager.canUndo())
        val undone1 = manager.undo(states.last())
        assertEquals("Should undo to second last state", states[4], undone1)
        
        assertTrue("Should be able to undo again", manager.canUndo())
        val undone2 = manager.undo(undone1)
        assertEquals("Should undo to third last state", states[3], undone2)
        
        assertTrue("Should be able to undo once more", manager.canUndo())
        val undone3 = manager.undo(undone2)
        assertEquals("Should undo to fourth last state", states[2], undone3)
        
        // Should not be able to undo further (first state was removed)
        assertFalse("Should not be able to undo further", manager.canUndo())
    }

    @Test
    fun `canUndo should return false for single state`() {
        val manager = UndoRedoManager(5)
        val state = DrawingState(shapes = listOf(Shape.Point(Vec2(0f, 0f))))
        
        manager.push(state)
        
        assertFalse("Should not be able to undo with single state", manager.canUndo())
    }

    @Test
    fun `canRedo should return false initially`() {
        val manager = UndoRedoManager(5)
        val state = DrawingState(shapes = listOf(Shape.Point(Vec2(0f, 0f))))
        
        manager.push(state)
        
        assertFalse("Should not be able to redo initially", manager.canRedo())
    }

    @Test
    fun `multiple undo and redo operations should work correctly`() {
        val manager = UndoRedoManager(5)
        val states = (0..3).map { i ->
            DrawingState(shapes = listOf(Shape.Point(Vec2(i.toFloat(), i.toFloat()))))
        }
        
        states.forEach { manager.push(it) }
        
        // Undo twice
        val undone1 = manager.undo(states.last())
        val undone2 = manager.undo(undone1)
        
        // Redo once
        val redone1 = manager.redo(undone2)
        assertEquals("Should redo to second last state", states[2], redone1)
        
        // Redo again
        val redone2 = manager.redo(redone1)
        assertEquals("Should redo to last state", states.last(), redone2)
        
        // Should not be able to redo further
        assertFalse("Should not be able to redo further", manager.canRedo())
    }
}

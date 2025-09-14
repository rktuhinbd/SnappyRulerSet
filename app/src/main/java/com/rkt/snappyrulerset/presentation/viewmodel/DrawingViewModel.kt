package com.rkt.snappyrulerset.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.rkt.snappyrulerset.domain.entity.DrawingState
import com.rkt.snappyrulerset.domain.entity.UndoRedoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for the drawing screen that manages the drawing state and provides
 * operations for updating, undoing, redoing, and clearing the drawing.
 */
class DrawingViewModel : ViewModel() {
    private val undoRedoManager = UndoRedoManager(max = 50)

    private val _state = MutableStateFlow(DrawingState())
    val state: StateFlow<DrawingState> = _state

    init {
        undoRedoManager.push(_state.value)
    }

    /**
     * Updates the drawing state using the provided transformation function
     */
    fun update(block: (DrawingState) -> DrawingState) {
        val s = block(_state.value)
        _state.value = s
        undoRedoManager.push(s)
    }

    /**
     * Undoes the last action if possible
     */
    fun undo() {
        if (undoRedoManager.canUndo()) {
            _state.value = undoRedoManager.undo(_state.value)
        }
    }

    /**
     * Redoes the last undone action if possible
     */
    fun redo() {
        if (undoRedoManager.canRedo()) {
            _state.value = undoRedoManager.redo(_state.value)
        }
    }

    /**
     * Clears all shapes from the drawing
     */
    fun clear() {
        val clearedState = _state.value.copy(shapes = emptyList())
        _state.value = clearedState
        undoRedoManager.push(clearedState)
    }
}
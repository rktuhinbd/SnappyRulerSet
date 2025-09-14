package com.rkt.snappyrulerset.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.rkt.snappyrulerset.domain.entity.DrawingState
import com.rkt.snappyrulerset.domain.entity.UndoRedoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DrawingViewModel : ViewModel() {
    private val undoRedoManager = UndoRedoManager(max = 50)

    private val _state = MutableStateFlow(DrawingState())
    val state: StateFlow<DrawingState> = _state

    init {
        undoRedoManager.push(_state.value)
    }

    fun update(block: (DrawingState) -> DrawingState) {
        val s = block(_state.value)
        _state.value = s
        undoRedoManager.push(s)
    }

    fun undo() {
        if (undoRedoManager.canUndo()) {
            _state.value = undoRedoManager.undo(_state.value)
        }
    }

    fun redo() {
        if (undoRedoManager.canRedo()) {
            _state.value = undoRedoManager.redo(_state.value)
        }
    }

    fun clear() {
        val clearedState = _state.value.copy(shapes = emptyList())
        _state.value = clearedState
        undoRedoManager.push(clearedState)
    }
}
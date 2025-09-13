package com.rkt.snappyrulerset.viewmodel

import androidx.lifecycle.ViewModel
import com.rkt.snappyrulerset.model.DrawingState
import com.rkt.snappyrulerset.history.History
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DrawingViewModel : ViewModel() {
    private val history = History(max = 50)

    private val _state = MutableStateFlow(DrawingState())
    val state: StateFlow<DrawingState> = _state

    init {
        history.push(_state.value)
    }

    fun update(block: (DrawingState) -> DrawingState) {
        val s = block(_state.value)
        _state.value = s
        history.push(s)
    }

    fun undo() {
        if (history.canUndo()) {
            _state.value = history.undo(_state.value)
        }
    }

    fun redo() {
        if (history.canRedo()) {
            _state.value = history.redo(_state.value)
        }
    }
}
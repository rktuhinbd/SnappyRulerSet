package com.rkt.snappyrulerset.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.rkt.snappyrulerset.data.local.SettingsManager
import com.rkt.snappyrulerset.domain.entity.DrawingState
import com.rkt.snappyrulerset.domain.entity.UndoRedoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for the drawing screen that manages the drawing state and provides
 * operations for updating, undoing, redoing, and clearing the drawing.
 */
class DrawingViewModel(private val context: Context? = null) : ViewModel() {
    private val undoRedoManager = UndoRedoManager(max = 50)
    private val settingsManager = context?.let { SettingsManager(it) }

    private val _state = MutableStateFlow(
        settingsManager?.loadSettings() ?: DrawingState()
    )
    val state: StateFlow<DrawingState> = _state

    init {
        undoRedoManager.push(_state.value)
    }

    /**
     * Updates the drawing state using the provided transformation function
     */
    fun update(block: (DrawingState) -> DrawingState) {
        val oldState = _state.value
        val newState = block(oldState)
        _state.value = newState
        undoRedoManager.push(newState)
        
        // Save settings to SharedPreferences if they changed
        settingsManager?.let { manager ->
            if (hasSettingsChanged(oldState, newState)) {
                manager.saveSettings(newState)
            }
        }
    }
    
    /**
     * Check if any settings have changed
     */
    private fun hasSettingsChanged(oldState: DrawingState, newState: DrawingState): Boolean {
        return oldState.theme != newState.theme ||
               oldState.gridSpacingMm != newState.gridSpacingMm ||
               oldState.snapRadiusPx != newState.snapRadiusPx ||
               oldState.snapping != newState.snapping ||
               oldState.hapticFeedback != newState.hapticFeedback ||
               oldState.showGrid != newState.showGrid ||
               oldState.showSnapIndicators != newState.showSnapIndicators ||
               oldState.showMeasurements != newState.showMeasurements
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
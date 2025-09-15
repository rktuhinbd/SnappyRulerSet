package com.rkt.snappyrulerset.presentation.ui.components

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Manages haptic feedback throughout the application
 */
class HapticFeedbackManager {
    
    /**
     * Triggers haptic feedback for snap events
     */
    fun triggerSnapFeedback(haptic: HapticFeedback, enabled: Boolean = true) {
        if (enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    
    /**
     * Triggers haptic feedback for tool selection
     */
    fun triggerToolSelectionFeedback(haptic: HapticFeedback, enabled: Boolean = true) {
        if (enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    
    /**
     * Triggers haptic feedback for button presses
     */
    fun triggerButtonPressFeedback(haptic: HapticFeedback, enabled: Boolean = true) {
        if (enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    
    /**
     * Triggers haptic feedback for drawing actions
     */
    fun triggerDrawingFeedback(haptic: HapticFeedback, enabled: Boolean = true) {
        if (enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    
    /**
     * Triggers haptic feedback for undo/redo actions
     */
    fun triggerUndoRedoFeedback(haptic: HapticFeedback, enabled: Boolean = true) {
        if (enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    
    /**
     * Triggers haptic feedback for calibration events
     */
    fun triggerCalibrationFeedback(haptic: HapticFeedback, enabled: Boolean = true) {
        if (enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    
    /**
     * Triggers haptic feedback for export actions
     */
    fun triggerExportFeedback(haptic: HapticFeedback, enabled: Boolean = true) {
        if (enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    
    /**
     * Triggers haptic feedback for error states
     */
    fun triggerErrorFeedback(haptic: HapticFeedback, enabled: Boolean = true) {
        if (enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    
    /**
     * Triggers haptic feedback for success states
     */
    fun triggerSuccessFeedback(haptic: HapticFeedback, enabled: Boolean = true) {
        if (enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
}

// Global instance for easy access
val hapticManager = HapticFeedbackManager()

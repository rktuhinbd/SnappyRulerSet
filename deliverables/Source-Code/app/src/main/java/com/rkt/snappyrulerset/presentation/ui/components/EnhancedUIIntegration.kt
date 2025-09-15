package com.rkt.snappyrulerset.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.rkt.snappyrulerset.data.local.CalibrationData
import com.rkt.snappyrulerset.data.local.FrameRateMonitor
import com.rkt.snappyrulerset.domain.entity.DrawingState
import com.rkt.snappyrulerset.domain.entity.Vec2

/**
 * Integrated enhanced UI components for the drawing screen
 */
@Composable
fun EnhancedUIOverlay(
    state: DrawingState,
    calibrationData: CalibrationData,
    frameRateMonitor: FrameRateMonitor,
    currentPosition: Vec2? = null,
    lastPosition: Vec2? = null,
    currentAngle: Float? = null,
    currentRadius: Float? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Enhanced Grid Visualization
        if (state.showGrid) {
            EnhancedGridVisualization(
                gridSpacingMm = state.gridSpacingMm,
                viewport = state.viewport,
                isVisible = state.showGrid,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Enhanced Precision HUD
        if (state.showMeasurements) {
            EnhancedPrecisionHUD(
                calibrationData = calibrationData,
                frameRateMonitor = frameRateMonitor,
                currentPosition = currentPosition,
                lastPosition = lastPosition,
                currentAngle = currentAngle,
                currentRadius = currentRadius,
                isVisible = state.showMeasurements,
                modifier = Modifier.align(Alignment.TopStart)
            )
        }
    }
}

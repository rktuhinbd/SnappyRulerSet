package com.rkt.snappyrulerset.data.local

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.system.measureTimeMillis

class FrameRateMonitor {
    private var frameCount = 0
    private var lastFpsTime = System.currentTimeMillis()
    private var currentFps = 0f
    private val fpsHistory = mutableListOf<Float>()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun startFrame() {
        frameCount++
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastFpsTime >= 1000) {
            currentFps = frameCount * 1000f / (currentTime - lastFpsTime)
            fpsHistory.add(currentFps)
            if (fpsHistory.size > 60) fpsHistory.removeAt(0) // Keep last 60 seconds

            frameCount = 0
            lastFpsTime = currentTime
        }
    }

    fun getCurrentFps(): Float = currentFps

    fun getAverageFps(): Float = if (fpsHistory.isNotEmpty()) fpsHistory.average().toFloat() else 0f

    fun getMinFps(): Float = if (fpsHistory.isNotEmpty()) fpsHistory.minOrNull() ?: 0f else 0f

    fun isPerformanceGood(): Boolean = getAverageFps() >= 55f && getMinFps() >= 45f

    fun measureOperation(name: String, operation: () -> Unit): Long {
        return measureTimeMillis(operation)
    }

    fun getPerformanceReport(): String {
        return buildString {
            appendLine("Performance Report:")
            appendLine("Current FPS: ${String.format("%.1f", currentFps)}")
            appendLine("Average FPS: ${String.format("%.1f", getAverageFps())}")
            appendLine("Min FPS: ${String.format("%.1f", getMinFps())}")
            appendLine("Performance Status: ${if (isPerformanceGood()) "Good" else "Needs Optimization"}")
        }
    }

    fun cleanup() {
        scope.cancel()
    }
}
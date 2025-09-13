package com.rkt.snappyrulerset.calibration

import android.content.Context
import android.content.SharedPreferences
import android.util.DisplayMetrics
import kotlin.math.abs

data class CalibrationData(
    val dpi: Float,
    val mmPerPx: Float,
    val isCalibrated: Boolean = false,
    val calibrationDate: Long = System.currentTimeMillis()
)

class CalibrationManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("calibration", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_DPI = "dpi"
        private const val KEY_MM_PER_PX = "mm_per_px"
        private const val KEY_IS_CALIBRATED = "is_calibrated"
        private const val KEY_CALIBRATION_DATE = "calibration_date"
        
        // Standard DPI values for common devices
        private val STANDARD_DPIS = mapOf(
            "ldpi" to 120f,
            "mdpi" to 160f,
            "hdpi" to 240f,
            "xhdpi" to 320f,
            "xxhdpi" to 480f,
            "xxxhdpi" to 640f
        )
    }
    
    fun getCalibrationData(): CalibrationData {
        val dpi = prefs.getFloat(KEY_DPI, getDefaultDpi())
        val mmPerPx = prefs.getFloat(KEY_MM_PER_PX, 25.4f / dpi)
        val isCalibrated = prefs.getBoolean(KEY_IS_CALIBRATED, false)
        val calibrationDate = prefs.getLong(KEY_CALIBRATION_DATE, 0L)
        
        return CalibrationData(dpi, mmPerPx, isCalibrated, calibrationDate)
    }
    
    fun calibrateWithKnownLength(measuredPixels: Float, actualMm: Float): CalibrationData {
        val mmPerPx = actualMm / measuredPixels
        val dpi = 25.4f / mmPerPx
        
        val calibrationData = CalibrationData(dpi, mmPerPx, true)
        saveCalibration(calibrationData)
        
        return calibrationData
    }
    
    fun calibrateWithDpi(dpi: Float): CalibrationData {
        val mmPerPx = 25.4f / dpi
        val calibrationData = CalibrationData(dpi, mmPerPx, true)
        saveCalibration(calibrationData)
        
        return calibrationData
    }
    
    fun resetCalibration() {
        prefs.edit().clear().apply()
    }
    
    private fun getDefaultDpi(): Float {
        val displayMetrics = context.resources.displayMetrics
        val density = displayMetrics.densityDpi.toFloat()
        
        // Find closest standard DPI
        return STANDARD_DPIS.values.minByOrNull { abs(it - density) } ?: density
    }
    
    private fun saveCalibration(data: CalibrationData) {
        prefs.edit()
            .putFloat(KEY_DPI, data.dpi)
            .putFloat(KEY_MM_PER_PX, data.mmPerPx)
            .putBoolean(KEY_IS_CALIBRATED, data.isCalibrated)
            .putLong(KEY_CALIBRATION_DATE, data.calibrationDate)
            .apply()
    }
    
    fun getCalibrationAccuracy(): String {
        val data = getCalibrationData()
        return if (data.isCalibrated) {
            "Calibrated (${String.format("%.1f", data.dpi)} DPI)"
        } else {
            "Default (${String.format("%.1f", data.dpi)} DPI)"
        }
    }
    
    fun needsRecalibration(): Boolean {
        val data = getCalibrationData()
        if (!data.isCalibrated) return false
        
        val daysSinceCalibration = (System.currentTimeMillis() - data.calibrationDate) / (1000 * 60 * 60 * 24)
        return daysSinceCalibration > 30 // Suggest recalibration after 30 days
    }
}

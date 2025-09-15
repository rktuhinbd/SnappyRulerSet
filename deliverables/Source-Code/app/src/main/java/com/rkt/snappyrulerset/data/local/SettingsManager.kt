package com.rkt.snappyrulerset.data.local

import android.content.Context
import android.content.SharedPreferences
import com.rkt.snappyrulerset.domain.entity.DrawingState

/**
 * Manages application settings using SharedPreferences
 */
class SettingsManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "snappy_ruler_settings", 
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_THEME = "theme"
        private const val KEY_GRID_SPACING = "grid_spacing_mm"
        private const val KEY_SNAP_RADIUS = "snap_radius_px"
        private const val KEY_SNAPPING = "snapping"
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        private const val KEY_SHOW_GRID = "show_grid"
        private const val KEY_SHOW_SNAP_INDICATORS = "show_snap_indicators"
        private const val KEY_SHOW_MEASUREMENTS = "show_measurements"
        
        // Default values
        private const val DEFAULT_THEME = "light"
        private const val DEFAULT_GRID_SPACING = 5f
        private const val DEFAULT_SNAP_RADIUS = 16f
        private const val DEFAULT_SNAPPING = true
        private const val DEFAULT_HAPTIC_FEEDBACK = true
        private const val DEFAULT_SHOW_GRID = true
        private const val DEFAULT_SHOW_SNAP_INDICATORS = true
        private const val DEFAULT_SHOW_MEASUREMENTS = true
    }
    
    /**
     * Load settings from SharedPreferences and apply to DrawingState
     */
    fun loadSettings(): DrawingState {
        return DrawingState(
            theme = prefs.getString(KEY_THEME, DEFAULT_THEME) ?: DEFAULT_THEME,
            gridSpacingMm = prefs.getFloat(KEY_GRID_SPACING, DEFAULT_GRID_SPACING),
            snapRadiusPx = prefs.getFloat(KEY_SNAP_RADIUS, DEFAULT_SNAP_RADIUS),
            snapping = prefs.getBoolean(KEY_SNAPPING, DEFAULT_SNAPPING),
            hapticFeedback = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, DEFAULT_HAPTIC_FEEDBACK),
            showGrid = prefs.getBoolean(KEY_SHOW_GRID, DEFAULT_SHOW_GRID),
            showSnapIndicators = prefs.getBoolean(KEY_SHOW_SNAP_INDICATORS, DEFAULT_SHOW_SNAP_INDICATORS),
            showMeasurements = prefs.getBoolean(KEY_SHOW_MEASUREMENTS, DEFAULT_SHOW_MEASUREMENTS)
        )
    }
    
    /**
     * Save settings from DrawingState to SharedPreferences
     */
    fun saveSettings(state: DrawingState) {
        prefs.edit().apply {
            putString(KEY_THEME, state.theme)
            putFloat(KEY_GRID_SPACING, state.gridSpacingMm)
            putFloat(KEY_SNAP_RADIUS, state.snapRadiusPx)
            putBoolean(KEY_SNAPPING, state.snapping)
            putBoolean(KEY_HAPTIC_FEEDBACK, state.hapticFeedback)
            putBoolean(KEY_SHOW_GRID, state.showGrid)
            putBoolean(KEY_SHOW_SNAP_INDICATORS, state.showSnapIndicators)
            putBoolean(KEY_SHOW_MEASUREMENTS, state.showMeasurements)
            apply()
        }
    }
    
    /**
     * Save a specific setting
     */
    fun saveTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
    }
    
    fun saveGridSpacing(spacing: Float) {
        prefs.edit().putFloat(KEY_GRID_SPACING, spacing).apply()
    }
    
    fun saveSnapRadius(radius: Float) {
        prefs.edit().putFloat(KEY_SNAP_RADIUS, radius).apply()
    }
    
    fun saveSnapping(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SNAPPING, enabled).apply()
    }
    
    fun saveHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply()
    }
    
    fun saveShowGrid(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_GRID, show).apply()
    }
    
    fun saveShowSnapIndicators(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_SNAP_INDICATORS, show).apply()
    }
    
    fun saveShowMeasurements(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_MEASUREMENTS, show).apply()
    }
    
    /**
     * Reset all settings to default values
     */
    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }
}

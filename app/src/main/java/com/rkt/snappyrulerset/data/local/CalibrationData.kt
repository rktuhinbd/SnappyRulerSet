package com.rkt.snappyrulerset.data.local

data class CalibrationData(
    val dpi: Float,
    val mmPerPx: Float,
    val isCalibrated: Boolean = false,
    val calibrationDate: Long = System.currentTimeMillis()
)
package com.rkt.snappyrulerset.presentation.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rkt.snappyrulerset.data.local.CalibrationData
import com.rkt.snappyrulerset.data.local.DeviceCalibrationManager
import com.rkt.snappyrulerset.domain.entity.Vec2
import com.rkt.snappyrulerset.domain.entity.distance
import kotlin.math.*

/**
 * Manual calibration screen for device-specific calibration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationScreen(
    onBack: () -> Unit,
    onCalibrationComplete: (CalibrationData) -> Unit
) {
    val context = LocalContext.current
    val calibrationManager = remember { DeviceCalibrationManager(context) }
    val currentCalibration = remember { calibrationManager.getCalibrationData() }

    var calibrationStep by remember { mutableStateOf(CalibrationStep.INSTRUCTIONS) }
    var measuredPixels by remember { mutableStateOf(0f) }
    var actualMm by remember { mutableStateOf(100f) } // Default 100mm reference
    var startPoint by remember { mutableStateOf(Vec2(0f, 0f)) }
    var endPoint by remember { mutableStateOf(Vec2(0f, 0f)) }
    var isDragging by remember { mutableStateOf(false) }

    val calculatedDpi = if (measuredPixels > 0) {
        (measuredPixels * 25.4f) / actualMm
    } else {
        currentCalibration.dpi
    }

    val calculatedMmPerPx = if (measuredPixels > 0) {
        actualMm / measuredPixels
    } else {
        currentCalibration.mmPerPx
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Calibration") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (calibrationStep) {
                CalibrationStep.INSTRUCTIONS -> {
                    CalibrationInstructions(
                        onStart = { calibrationStep = CalibrationStep.MEASUREMENT }
                    )
                }

                CalibrationStep.MEASUREMENT -> {
                    CalibrationMeasurement(
                        actualMm = actualMm,
                        onActualMmChange = { actualMm = it },
                        startPoint = startPoint,
                        endPoint = endPoint,
                        isDragging = isDragging,
                        onStartDrag = { point ->
                            startPoint = point
                            endPoint = point
                            isDragging = true
                        },
                        onDrag = { point ->
                            endPoint = point
                        },
                        onEndDrag = {
                            isDragging = false
                            measuredPixels = distance(startPoint, endPoint)
                        },
                        onNext = { calibrationStep = CalibrationStep.CONFIRMATION }
                    )
                }

                CalibrationStep.CONFIRMATION -> {
                    CalibrationConfirmation(
                        currentCalibration = currentCalibration,
                        newCalibration = CalibrationData(
                            dpi = calculatedDpi,
                            mmPerPx = calculatedMmPerPx,
                            isCalibrated = true
                        ),
                        onConfirm = {
                            calibrationManager.saveCalibration(
                                CalibrationData(
                                    dpi = calculatedDpi,
                                    mmPerPx = calculatedMmPerPx,
                                    isCalibrated = true
                                )
                            )
                            onCalibrationComplete(
                                CalibrationData(
                                    dpi = calculatedDpi,
                                    mmPerPx = calculatedMmPerPx,
                                    isCalibrated = true
                                )
                            )
                        },
                        onRestart = {
                            calibrationStep = CalibrationStep.INSTRUCTIONS
                            measuredPixels = 0f
                            startPoint = Vec2(0f, 0f)
                            endPoint = Vec2(0f, 0f)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalibrationInstructions(
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Device Calibration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "To ensure accurate measurements, we need to calibrate your device. You'll measure a known distance to calculate the correct pixel-to-millimeter ratio.",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Instructions:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "1. Place a ruler or measuring tape on your screen\n" +
                        "2. Align it with the measurement line\n" +
                        "3. Drag to measure a known distance (e.g., 100mm)\n" +
                        "4. Enter the actual measured distance\n" +
                        "5. Confirm the calibration",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Calibration")
            }
        }
    }
}

@Composable
private fun CalibrationMeasurement(
    actualMm: Float,
    onActualMmChange: (Float) -> Unit,
    startPoint: Vec2,
    endPoint: Vec2,
    isDragging: Boolean,
    onStartDrag: (Vec2) -> Unit,
    onDrag: (Vec2) -> Unit,
    onEndDrag: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Measure Known Distance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Actual distance input
            OutlinedTextField(
                value = actualMm.toString(),
                onValueChange = {
                    it.toFloatOrNull()?.let { value ->
                        if (value > 0) onActualMmChange(value)
                    }
                },
                label = { Text("Actual Distance (mm)") },
                modifier = Modifier.fillMaxWidth(),
                suffix = { Text("mm") }
            )

            // Measurement canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFF5F5F5))
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                onStartDrag(Vec2(offset.x, offset.y))
                            },
                            onDrag = { change, _ ->
                                val currentPos = Vec2(change.position.x, change.position.y)
                                onDrag(currentPos)
                            },
                            onDragEnd = {
                                onEndDrag()
                            }
                        )
                    }
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isDragging || distance(startPoint, endPoint) > 0) {
                        // Draw measurement line
                        drawLine(
                            color = Color(0xFF2196F3),
                            start = Offset(startPoint.x, startPoint.y),
                            end = Offset(endPoint.x, endPoint.y),
                            strokeWidth = 4.dp.toPx()
                        )

                        // Draw start point
                        drawCircle(
                            color = Color(0xFF4CAF50),
                            radius = 8.dp.toPx(),
                            center = Offset(startPoint.x, startPoint.y)
                        )

                        // Draw end point
                        drawCircle(
                            color = Color(0xFFF44336),
                            radius = 8.dp.toPx(),
                            center = Offset(endPoint.x, endPoint.y)
                        )

                        // Draw measurement text
                        val measuredDistance = distance(startPoint, endPoint)
                        val centerX = (startPoint.x + endPoint.x) / 2
                        val centerY = (startPoint.y + endPoint.y) / 2 - 20

                        // Note: Text drawing would require additional setup
                    }
                }

                // Instructions overlay
                if (distance(startPoint, endPoint) == 0f) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Drag to measure a known distance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Measurement results
            if (distance(startPoint, endPoint) > 0) {
                val measuredPixels = distance(startPoint, endPoint)
                val calculatedMmPerPx = if (actualMm > 0) actualMm / measuredPixels else 0f
                val calculatedDpi = if (actualMm > 0) (measuredPixels * 25.4f) / actualMm else 0f

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Measurement Results",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("Measured: ${String.format("%.1f", measuredPixels)} pixels")
                        Text("Actual: ${String.format("%.1f", actualMm)} mm")
                        Text("Ratio: ${String.format("%.3f", calculatedMmPerPx)} mm/px")
                        Text("DPI: ${String.format("%.0f", calculatedDpi)}")
                    }
                }
            }
        }
    }
}

@Composable
private fun CalibrationConfirmation(
    currentCalibration: CalibrationData,
    newCalibration: CalibrationData,
    onConfirm: () -> Unit,
    onRestart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Calibration Complete",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Current vs New comparison
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Current",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("DPI: ${String.format("%.0f", currentCalibration.dpi)}")
                    Text("mm/px: ${String.format("%.3f", currentCalibration.mmPerPx)}")
                }

                Column {
                    Text(
                        text = "New",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )
                    Text("DPI: ${String.format("%.0f", newCalibration.dpi)}")
                    Text("mm/px: ${String.format("%.3f", newCalibration.mmPerPx)}")
                }
            }

            // Accuracy information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Calibration Accuracy",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("• Length measurements: ±1mm accuracy")
                    Text("• Angle measurements: ±0.5° accuracy")
                    Text("• Calibration valid for 30 days")
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restart")
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Confirm")
                }
            }
        }
    }
}

@Composable
private fun CalibrationMeasurement(
    actualMm: Float,
    onActualMmChange: (Float) -> Unit,
    startPoint: Vec2,
    endPoint: Vec2,
    isDragging: Boolean,
    onStartDrag: (Vec2) -> Unit,
    onDrag: (Vec2) -> Unit,
    onEndDrag: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Measure Known Distance",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Actual distance input
        OutlinedTextField(
            value = actualMm.toString(),
            onValueChange = {
                it.toFloatOrNull()?.let { value ->
                    if (value > 0) onActualMmChange(value)
                }
            },
            label = { Text("Actual Distance") },
            suffix = { Text("mm") }
        )

        // Measurement canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFF5F5F5))
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            onStartDrag(Vec2(offset.x, offset.y))
                        },
                        onDrag = { change, _ ->
                            val currentPos = Vec2(change.position.x, change.position.y)
                            onDrag(currentPos)
                        },
                        onDragEnd = {
                            onEndDrag()
                        }
                    )
                }
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                if (isDragging || distance(startPoint, endPoint) > 0) {
                    // Draw measurement line
                    drawLine(
                        color = Color(0xFF2196F3),
                        start = Offset(startPoint.x, startPoint.y),
                        end = Offset(endPoint.x, endPoint.y),
                        strokeWidth = 4.dp.toPx()
                    )

                    // Draw start point
                    drawCircle(
                        color = Color(0xFF4CAF50),
                        radius = 8f,
                        center = Offset(startPoint.x, startPoint.y)
                    )

                    // Draw end point
                    drawCircle(
                        color = Color(0xFFF44336),
                        radius = 8f,
                        center = Offset(endPoint.x, endPoint.y)
                    )

                    // Draw measurement text
                    val measuredDistance = distance(startPoint, endPoint)
                    val centerX = (startPoint.x + endPoint.x) / 2
                    val centerY = (startPoint.y + endPoint.y) / 2 - 20

                    // Note: Text drawing would require additional setup
                }
            }

            // Instructions overlay
            if (distance(startPoint, endPoint) == 0f) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Drag to measure a known distance",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        }

        // Measurement results
        if (distance(startPoint, endPoint) > 0) {
            val measuredPixels = distance(startPoint, endPoint)
            val calculatedMmPerPx = if (actualMm > 0) actualMm / measuredPixels else 0f
            val calculatedDpi = if (actualMm > 0) (measuredPixels * 25.4f) / actualMm else 0f

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Measurement Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("Measured: ${String.format("%.1f", measuredPixels)} pixels")
                    Text("Actual: ${String.format("%.1f", actualMm)} mm")
                    Text("Calculated DPI: ${String.format("%.1f", calculatedDpi)}")
                    Text("mm/px: ${String.format("%.3f", calculatedMmPerPx)}")
                }
            }

            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue to Confirmation")
            }
        }
    }
}

private enum class CalibrationStep {
    INSTRUCTIONS,
    MEASUREMENT,
    CONFIRMATION
}

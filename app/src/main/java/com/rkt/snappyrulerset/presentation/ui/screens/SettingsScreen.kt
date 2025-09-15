package com.rkt.snappyrulerset.presentation.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rkt.snappyrulerset.data.local.CalibrationData
import com.rkt.snappyrulerset.data.local.DeviceCalibrationManager
import com.rkt.snappyrulerset.domain.entity.DrawingState
import com.rkt.snappyrulerset.presentation.viewmodel.DrawingViewModel

/**
 * Comprehensive Settings Screen with stunning UI/UX
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: DrawingViewModel,
    onBack: () -> Unit,
    onNavigateToCalibration: () -> Unit
) {
    val context = LocalContext.current
    val calibrationManager = remember { DeviceCalibrationManager(context) }
    val calibrationData = remember { calibrationManager.getCalibrationData() }
    val state by viewModel.state.collectAsState()

    var showCalibrationDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    // Handle back navigation
    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Drawing Settings
            SettingsSection(
                title = "Drawing Settings",
                icon = Icons.Default.Settings,
                description = "Configure drawing behavior and precision"
            ) {
                GridSettingsCard(
                    gridSpacingMm = state.gridSpacingMm,
                    onGridSpacingChange = { newSpacing ->
                        viewModel.update { it.copy(gridSpacingMm = newSpacing) }
                    }
                )

                SnapSettingsCard(
                    snapRadiusPx = state.snapRadiusPx,
                    snapping = state.snapping,
                    onSnapRadiusChange = { newRadius ->
                        viewModel.update { it.copy(snapRadiusPx = newRadius) }
                    },
                    onSnappingToggle = { enabled ->
                        viewModel.update { it.copy(snapping = enabled) }
                    }
                )
            }

            // Visual Settings
            SettingsSection(
                title = "Visual Preferences",
                icon = Icons.Default.Visibility,
                description = "Customize appearance and visual feedback"
            ) {
                VisualSettingsCard(
                    theme = state.theme,
                    showGrid = state.showGrid,
                    showSnapIndicators = state.showSnapIndicators,
                    showMeasurements = state.showMeasurements,
                    onThemeChange = { newTheme ->
                        viewModel.update { it.copy(theme = newTheme) }
                    },
                    onShowGridToggle = { enabled ->
                        viewModel.update { it.copy(showGrid = enabled) }
                    },
                    onShowSnapIndicatorsToggle = { enabled ->
                        viewModel.update { it.copy(showSnapIndicators = enabled) }
                    },
                    onShowMeasurementsToggle = { enabled ->
                        viewModel.update { it.copy(showMeasurements = enabled) }
                    }
                )
            }

            // Behavior Settings
            SettingsSection(
                title = "App Behavior",
                icon = Icons.Default.Tune,
                description = "Configure app behavior and interactions"
            ) {
                BehaviorSettingsCard(
                    hapticFeedback = state.hapticFeedback,
                    onHapticFeedbackToggle = { enabled ->
                        viewModel.update { it.copy(hapticFeedback = enabled) }
                    }
                )
            }

            // Calibration Settings
            SettingsSection(
                title = "Calibration",
                icon = Icons.Default.Straighten,
                description = "Device calibration for accurate measurements"
            ) {
                CalibrationSettingsCard(
                    calibrationData = calibrationData,
                    onRecalibrate = { showCalibrationDialog = true }
                )
            }

            // Advanced Settings
            SettingsSection(
                title = "Advanced",
                icon = Icons.Default.Build,
                description = "Advanced options and maintenance"
            ) {
                AdvancedSettingsCard(
                    onResetSettings = { showResetDialog = true }
                )
            }

            // App Information
            AppInfoCard()
        }
    }

    // Calibration Dialog
    if (showCalibrationDialog) {
        AlertDialog(
            onDismissRequest = { showCalibrationDialog = false },
            title = { Text("Recalibrate Device") },
            text = { Text("This will open the calibration screen to measure a known distance and update your device's calibration settings.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCalibrationDialog = false
                        onNavigateToCalibration()
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCalibrationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Settings Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Settings") },
            text = { Text("This will reset all settings to their default values. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Reset to default settings including calibration
                        viewModel.update {
                            DrawingState(
                                gridSpacingMm = 5f,
                                snapRadiusPx = 16f,
                                snapping = true,
                                hapticFeedback = true,
                                showGrid = true,
                                showSnapIndicators = true,
                                showMeasurements = true,
                                theme = "light"
                            )
                        }

                        // Reset calibration data
                        val deviceCalibrationManager = DeviceCalibrationManager(context)
                        deviceCalibrationManager.resetCalibration()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun GridSettingsCard(
    gridSpacingMm: Float,
    onGridSpacingChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Grid Spacing",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Current: ${String.format("%.1f", gridSpacingMm)} mm",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val commonSpacings = listOf(1f, 2f, 5f, 10f, 20f)
                commonSpacings.forEach { spacing ->
                    FilterChip(
                        onClick = { onGridSpacingChange(spacing) },
                        label = { Text("${spacing.toInt()}mm") },
                        selected = gridSpacingMm == spacing,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Custom spacing input
            OutlinedTextField(
                value = gridSpacingMm.toString(),
                onValueChange = {
                    it.toFloatOrNull()?.let { value ->
                        if (value > 0 && value <= 100) {
                            onGridSpacingChange(value)
                        }
                    }
                },
                label = { Text("Custom (mm)") },
                modifier = Modifier.fillMaxWidth(),
                suffix = { Text("mm") }
            )
        }
    }
}

@Composable
private fun SnapSettingsCard(
    snapRadiusPx: Float,
    snapping: Boolean,
    onSnapRadiusChange: (Float) -> Unit,
    onSnappingToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Snapping",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            // Snapping toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Snapping")
                Switch(
                    checked = snapping,
                    onCheckedChange = onSnappingToggle
                )
            }

            AnimatedVisibility(visible = snapping) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Snap Radius: ${String.format("%.0f", snapRadiusPx)} px",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Slider(
                        value = snapRadiusPx,
                        onValueChange = onSnapRadiusChange,
                        valueRange = 8f..32f,
                        steps = 11, // 2px increments
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("8px", style = MaterialTheme.typography.bodySmall)
                        Text("32px", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun VisualSettingsCard(
    theme: String,
    showGrid: Boolean,
    showSnapIndicators: Boolean,
    showMeasurements: Boolean,
    onThemeChange: (String) -> Unit,
    onShowGridToggle: (Boolean) -> Unit,
    onShowSnapIndicatorsToggle: (Boolean) -> Unit,
    onShowMeasurementsToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Visual Preferences",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            // Theme selection
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val themes = listOf("light", "dark")
                themes.forEach { themeOption ->
                    FilterChip(
                        onClick = { onThemeChange(themeOption) },
                        label = { Text(themeOption.replaceFirstChar { it.uppercase() }) },
                        selected = theme == themeOption,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Grid visibility
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show Grid")
                Switch(
                    checked = showGrid,
                    onCheckedChange = onShowGridToggle
                )
            }

            // Snap indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show Snap Indicators")
                Switch(
                    checked = showSnapIndicators,
                    onCheckedChange = onShowSnapIndicatorsToggle
                )
            }

            // Measurement labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show Measurement Labels")
                Switch(
                    checked = showMeasurements,
                    onCheckedChange = onShowMeasurementsToggle
                )
            }
        }
    }
}

@Composable
private fun BehaviorSettingsCard(
    hapticFeedback: Boolean,
    onHapticFeedbackToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "App Behavior",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            // Haptic feedback toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Haptic Feedback")
                Switch(
                    checked = hapticFeedback,
                    onCheckedChange = onHapticFeedbackToggle
                )
            }
        }
    }
}

@Composable
private fun CalibrationSettingsCard(
    calibrationData: CalibrationData,
    onRecalibrate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Device Calibration",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "DPI: ${String.format("%.0f", calibrationData.dpi)}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "mm/px: ${String.format("%.3f", calibrationData.mmPerPx)}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Status: ${if (calibrationData.isCalibrated) "Calibrated" else "Default"}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (calibrationData.isCalibrated)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondary
            )

            Button(
                onClick = onRecalibrate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Recalibrate Device")
            }
        }
    }
}

@Composable
private fun AdvancedSettingsCard(
    onResetSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Advanced Options",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedButton(
                onClick = onResetSettings,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Reset All Settings")
            }
        }
    }
}

@Composable
private fun AppInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "App Information",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Snappy Ruler Set v1.0",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Precision drawing app with intelligent snapping",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Built with Jetpack Compose",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

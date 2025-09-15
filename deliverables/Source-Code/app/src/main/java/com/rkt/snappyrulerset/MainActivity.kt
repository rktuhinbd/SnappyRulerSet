package com.rkt.snappyrulerset

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.rkt.snappyrulerset.presentation.ui.screens.CalibrationScreen
import com.rkt.snappyrulerset.presentation.ui.screens.DrawingScreen
import com.rkt.snappyrulerset.presentation.ui.screens.SettingsScreen
import com.rkt.snappyrulerset.presentation.ui.theme.SnappyRulerSetTheme
import com.rkt.snappyrulerset.presentation.viewmodel.DrawingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf(AppScreen.Drawing) }
    val context = LocalContext.current
    val viewModel = remember { DrawingViewModel(context) }
    val state by viewModel.state.collectAsState()

    // Apply the theme based on the drawing state
    SnappyRulerSetTheme(darkTheme = state.theme == "dark") {
        when (currentScreen) {
            AppScreen.Drawing -> {
                DrawingScreen(
                    vm = viewModel,
                    onNavigateToCalibration = { currentScreen = AppScreen.Calibration },
                    onNavigateToSettings = { currentScreen = AppScreen.Settings }
                )
            }

            AppScreen.Calibration -> {
                CalibrationScreen(
                    onBack = { currentScreen = AppScreen.Settings },
                    onCalibrationComplete = { currentScreen = AppScreen.Settings }
                )
            }

            AppScreen.Settings -> {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = AppScreen.Drawing },
                    onNavigateToCalibration = { currentScreen = AppScreen.Calibration }
                )
            }
        }
    }
}

enum class AppScreen {
    Drawing,
    Calibration,
    Settings
}


@Preview(showBackground = true)
@Composable
private fun DrawingPreview() {
    SnappyRulerSetTheme {
        SnappyRulerSetTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                DrawingScreen()
            }
        }
    }
}
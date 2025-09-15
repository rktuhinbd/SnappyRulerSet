package com.rkt.snappyrulerset.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.rkt.snappyrulerset.domain.entity.Vec2
import kotlinx.coroutines.delay

/**
 * Manages smooth animations throughout the application
 */
object AnimationManager {
    
    /**
     * Animation specifications for different interactions
     */
    object Specs {
        val ToolSelection = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)
        val SnapFeedback = tween<Float>(durationMillis = 150, easing = FastOutSlowInEasing)
        val ButtonPress = tween<Float>(durationMillis = 100, easing = FastOutSlowInEasing)
        val DrawingAction = tween<Float>(durationMillis = 300, easing = LinearEasing)
        val ViewportTransition = tween<Float>(durationMillis = 500, easing = FastOutSlowInEasing)
        val HUDAppearance = tween<Float>(durationMillis = 400, easing = FastOutSlowInEasing)
    }
    
    /**
     * Animation values for different states
     */
    object Values {
        val SelectedScale = 1.1f
        val UnselectedScale = 1.0f
        val PressedScale = 0.95f
        val SnapPulseScale = 1.2f
        val HUDAlpha = 0.95f
        val ToolAlpha = 0.8f
    }
}

/**
 * Animated scale modifier for tool selection
 */
@Composable
fun Modifier.animatedToolSelection(
    isSelected: Boolean,
    isPressed: Boolean = false
): Modifier {
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> AnimationManager.Values.PressedScale
            isSelected -> AnimationManager.Values.SelectedScale
            else -> AnimationManager.Values.UnselectedScale
        },
        animationSpec = AnimationManager.Specs.ToolSelection,
        label = "toolSelection"
    )
    
    return this.scale(scale)
}

/**
 * Animated alpha modifier for tool visibility
 */
@Composable
fun Modifier.animatedToolVisibility(
    isVisible: Boolean
): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = AnimationManager.Specs.ToolSelection,
        label = "toolVisibility"
    )
    
    return this.alpha(alpha)
}

/**
 * Animated snap feedback
 */
@Composable
fun Modifier.animatedSnapFeedback(
    isSnapped: Boolean
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isSnapped) AnimationManager.Values.SnapPulseScale else 1f,
        animationSpec = AnimationManager.Specs.SnapFeedback,
        label = "snapFeedback"
    )
    
    return this.scale(scale)
}

/**
 * Animated button press feedback
 */
@Composable
fun Modifier.animatedButtonPress(
    isPressed: Boolean
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) AnimationManager.Values.PressedScale else 1f,
        animationSpec = AnimationManager.Specs.ButtonPress,
        label = "buttonPress"
    )
    
    return this.scale(scale)
}

/**
 * Animated HUD appearance
 */
@Composable
fun Modifier.animatedHUDAppearance(
    isVisible: Boolean
): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) AnimationManager.Values.HUDAlpha else 0f,
        animationSpec = AnimationManager.Specs.HUDAppearance,
        label = "hudAppearance"
    )
    
    return this.alpha(alpha)
}

/**
 * Animated viewport transition
 */
@Composable
fun Modifier.animatedViewportTransition(
    isTransitioning: Boolean
): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (isTransitioning) 0.7f else 1f,
        animationSpec = AnimationManager.Specs.ViewportTransition,
        label = "viewportTransition"
    )
    
    return this.alpha(alpha)
}

/**
 * Animated position for smooth tool movement
 */
@Composable
fun animatedToolPosition(
    targetPosition: Vec2,
    isAnimating: Boolean = true
): Vec2 {
    val animatedX by animateFloatAsState(
        targetValue = targetPosition.x,
        animationSpec = if (isAnimating) AnimationManager.Specs.DrawingAction else snap(),
        label = "toolPositionX"
    )
    
    val animatedY by animateFloatAsState(
        targetValue = targetPosition.y,
        animationSpec = if (isAnimating) AnimationManager.Specs.DrawingAction else snap(),
        label = "toolPositionY"
    )
    
    return Vec2(animatedX, animatedY)
}

/**
 * Animated rotation for smooth tool rotation
 */
@Composable
fun animatedToolRotation(
    targetRotation: Float,
    isAnimating: Boolean = true
): Float {
    return animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = if (isAnimating) AnimationManager.Specs.DrawingAction else snap(),
        label = "toolRotation"
    ).value
}

/**
 * Animated scale for smooth tool scaling
 */
@Composable
fun animatedToolScale(
    targetScale: Float,
    isAnimating: Boolean = true
): Float {
    return animateFloatAsState(
        targetValue = targetScale,
        animationSpec = if (isAnimating) AnimationManager.Specs.DrawingAction else snap(),
        label = "toolScale"
    ).value
}

/**
 * Animated snap tick with pulse effect
 */
@Composable
fun Modifier.animatedSnapTick(
    isVisible: Boolean,
    isPulsing: Boolean = false
): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = AnimationManager.Specs.SnapFeedback,
        label = "snapTickAlpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPulsing) AnimationManager.Values.SnapPulseScale else 1f,
        animationSpec = AnimationManager.Specs.SnapFeedback,
        label = "snapTickScale"
    )
    
    return this
        .alpha(alpha)
        .scale(scale)
}

/**
 * Animated measurement display
 */
@Composable
fun Modifier.animatedMeasurementDisplay(
    isVisible: Boolean
): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = AnimationManager.Specs.HUDAppearance,
        label = "measurementDisplay"
    )
    
    return this.alpha(alpha)
}

/**
 * Animated grid appearance
 */
@Composable
fun Modifier.animatedGridAppearance(
    isVisible: Boolean
): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 0.3f else 0f,
        animationSpec = AnimationManager.Specs.HUDAppearance,
        label = "gridAppearance"
    )
    
    return this.alpha(alpha)
}

/**
 * Animated snap indicator appearance
 */
@Composable
fun Modifier.animatedSnapIndicator(
    isVisible: Boolean,
    priority: Int = 1
): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) {
            when (priority) {
                1 -> 1f // Point snaps
                2 -> 0.8f // Segment snaps
                3 -> 0.6f // Grid snaps
                else -> 0.4f
            }
        } else 0f,
        animationSpec = AnimationManager.Specs.SnapFeedback,
        label = "snapIndicator"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) {
            when (priority) {
                1 -> 1.1f // Point snaps
                2 -> 1.0f // Segment snaps
                3 -> 0.9f // Grid snaps
                else -> 0.8f
            }
        } else 0.5f,
        animationSpec = AnimationManager.Specs.SnapFeedback,
        label = "snapIndicatorScale"
    )
    
    return this
        .alpha(alpha)
        .scale(scale)
}

/**
 * Animated calibration feedback
 */
@Composable
fun Modifier.animatedCalibrationFeedback(
    isCalibrated: Boolean
): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (isCalibrated) 1f else 0.7f,
        animationSpec = AnimationManager.Specs.HUDAppearance,
        label = "calibrationFeedback"
    )
    
    return this.alpha(alpha)
}

/**
 * Animated export feedback
 */
@Composable
fun Modifier.animatedExportFeedback(
    isExporting: Boolean
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isExporting) 0.9f else 1f,
        animationSpec = AnimationManager.Specs.ButtonPress,
        label = "exportFeedback"
    )
    
    return this.scale(scale)
}

/**
 * Animated undo/redo feedback
 */
@Composable
fun Modifier.animatedUndoRedoFeedback(
    isEnabled: Boolean
): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (isEnabled) 1f else 0.5f,
        animationSpec = AnimationManager.Specs.ButtonPress,
        label = "undoRedoFeedback"
    )
    
    return this.alpha(alpha)
}

/**
 * Animated theme transition
 */
@Composable
fun Modifier.animatedThemeTransition(
    isTransitioning: Boolean
): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (isTransitioning) 0.5f else 1f,
        animationSpec = AnimationManager.Specs.ViewportTransition,
        label = "themeTransition"
    )
    
    return this.alpha(alpha)
}

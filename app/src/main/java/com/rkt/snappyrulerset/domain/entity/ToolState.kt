package com.rkt.snappyrulerset.domain.entity

/**
 * Available drawing tools in the application
 */
enum class ToolKind { 
    Ruler, 
    SetSquare45, 
    SetSquare3060, 
    Protractor, 
    Compass 
}

/**
 * Modes for the compass tool
 */
enum class CompassMode { 
    Circle, 
    Arc 
}

/**
 * Represents the transformation (position, rotation, scale) of a drawing tool
 */
data class ToolTransform(
    val position: Vec2 = Vec2(0f, 0f),
    val rotationRad: Float = 0f,
    val scale: Float = 1f
)

/**
 * Represents the current state of the active drawing tool
 */
data class ToolState(
    val kind: ToolKind = ToolKind.Ruler,
    val transform: ToolTransform = ToolTransform(),
    val compassMode: CompassMode = CompassMode.Circle
)

/**
 * Represents the current viewport (pan and zoom) of the drawing canvas
 */
data class Viewport(
    val pan: Vec2 = Vec2(0f, 0f),
    val zoom: Float = 1f
)

/**
 * Represents the complete state of the drawing application
 */
data class DrawingState(
    val shapes: List<Shape> = emptyList(),
    val tool: ToolState = ToolState(),
    val viewport: Viewport = Viewport(),
    val snapping: Boolean = true,
    val gridSpacingMm: Float = 5f, // Configurable grid spacing
    val snapRadiusPx: Float = 16f // Base snap radius
)
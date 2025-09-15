# Snappy Ruler Set - Android Drawing App

A precision drawing application for Android that provides virtual geometry tools with intelligent snapping for accurate construction.

## Features

### Core Tools
- **Ruler**: Drag, rotate, and position with two fingers. Draw straight lines along its edge with snapping to common angles (0°, 30°, 45°, 60°, 90°) and existing line endpoints/midpoints.
- **Set Square**: Two variants (45° and 30°–60°). Edge-aligned drawing with snapping to canvas grid and existing segments.
- **Protractor**: Place over a vertex; measure angle between two rays. Snap readout to nearest 0.5°; "hard snap" at common angles (30/45/60/90/120/135/150/180).
- **Compass**: Set radius by dragging; draw circles and arcs snapping to intersections or points.

### Interactions & UX
- **Snappy feel**: Magnetic snapping with visual hints and gentle haptic feedback
- **Gestures**: One-finger pan, two-finger pinch to zoom, two-finger rotate selected tool
- **Long-press**: Toggle snap on/off temporarily
- **Precision HUD**: Real-time display of angle/length while drawing
- **Undo/Redo**: 50-step history with clean state management

### Export
- Save drawings to PNG/JPEG
- Share via Android share sheet
- Gallery integration with proper permissions

## Architecture Overview

### Key Classes

#### State Management
- **`DrawingViewModel`**: Main ViewModel managing drawing state and history
- **`DrawingState`**: Immutable state containing shapes, tools, viewport, and settings
- **`History`**: Undo/redo functionality with configurable history size

#### Geometry & Math
- **`Geometry.kt`**: Core vector math operations (Vec2, distance, angle calculations)
- **`Shape.kt`**: Shape definitions (Line, Circle, Arc, Point, Path)
- **`ToolState.kt`**: Tool definitions and transforms

#### Snapping System
- **`SnapEngine`**: Core snapping logic for angles, grid, and points
- **`SpatialIndex`**: Efficient spatial indexing for fast point queries
- **`SnapPoint.kt`**: Point collection utilities (endpoints, intersections, etc.)

#### UI Components
- **`DrawingScreen.kt`**: Main Compose UI with gesture handling and rendering
- **`PrecisionHud.kt`**: Real-time measurement display component

#### Export & Utilities
- **`Exporter.kt`**: Bitmap generation and file export functionality
- **`CalibrationManager.kt`**: Device-specific calibration for real-world units
- **`PerformanceMonitor.kt`**: FPS monitoring and performance optimization

### State Flow

```
User Input → DrawingScreen → DrawingViewModel → DrawingState
                ↓
        SnapEngine + SpatialIndex → Snapped Coordinates
                ↓
        Shape Creation → History Management → State Update
                ↓
        Canvas Rendering → Visual Feedback
```

### Rendering Loop

1. **Input Processing**: Gesture detection and coordinate transformation
2. **Snapping**: Multi-priority snapping system (points > segments > grid)
3. **State Update**: Immutable state updates with history tracking
4. **Rendering**: Compose Canvas with optimized drawing operations
5. **Performance Monitoring**: FPS tracking and optimization triggers

## Snapping Strategy & Data Structures

### Snapping Priority System
1. **Point Snaps** (Priority 1): Endpoints, midpoints, circle centers, arc endpoints
2. **Segment Snaps** (Priority 2): Closest point on existing line segments
3. **Grid Snaps** (Priority 3): Configurable grid spacing (default 5mm)

### Spatial Indexing
- **Grid-based spatial index** with 32px cells for efficient point queries
- **O(1) insertion** and **O(k) query** where k is the number of points in nearby cells
- **Dynamic snap radius** based on zoom level (6-28px range)

### Intersection Detection
- **Line-line intersections**: O(n²) pairwise intersection detection
- **Circle-line intersections**: Analytical solution with segment validation
- **Circle-circle intersections**: Standard geometric intersection formula

## Performance Notes

### Optimizations Implemented
- **Spatial indexing** for fast point queries
- **Limited grid rendering** (max 50 lines per direction)
- **Efficient shape rendering** with Compose Canvas
- **Memory management** with bounded history (50 steps)

### Performance Targets
- **60 FPS** during tool manipulation on mid-range devices (6GB RAM)
- **Dynamic snap radius** to maintain responsiveness at different zoom levels
- **Optimized rendering** with viewport culling and level-of-detail

### Trade-offs
- **Spatial index memory usage** vs. query performance
- **History size** (50 steps) vs. memory consumption
- **Grid rendering limits** vs. visual completeness

## Calibration Approach

### Device-Based Calibration
- **Default DPI detection** from system display metrics
- **Standard DPI mapping** (ldpi: 120, mdpi: 160, hdpi: 240, etc.)
- **Manual calibration** with known measurements
- **Persistent storage** with calibration date tracking

### Accuracy
- **Length display** in cm with 1mm granularity
- **Angle readouts** within ±0.5° accuracy
- **Hard snapping** at common angles for precision
- **Calibration validation** with 30-day expiration

## Technical Specifications

- **Platform**: Android (API 24+)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with StateFlow
- **Performance**: 60 FPS target on mid-range devices
- **Offline-first**: No network dependencies

## Testing

### Unit Tests
- **Geometry helpers**: Vector math, angle calculations, intersection detection
- **Snapping logic**: Grid snapping, angle snapping, spatial queries
- **State management**: History operations, state updates

### Instrumentation Tests
- **UI interactions**: Tool selection, mode switching, gesture handling
- **Export functionality**: File generation and sharing
- **Performance**: FPS monitoring and optimization triggers

## Build & Run

```bash
# Clone the repository
git clone <repository-url>
cd SnappyRuler

# Build the project
./gradlew assembleDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest

# Install on device
./gradlew installDebug
```

## Dependencies

- **Jetpack Compose**: Modern UI framework
- **AndroidX Core**: Core Android libraries
- **Coroutines**: Asynchronous programming
- **Material3**: Design system components

## License

This project is licensed under the MIT License - see the LICENSE file for details.

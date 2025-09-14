# Snappy Ruler Set - Test Suite

This directory contains comprehensive tests for the Snappy Ruler Set application.

## Test Structure

### Unit Tests
- **GeometryTest**: Tests for Vec2 operations, distance calculations, and geometric projections
- **SnappingEngineTest**: Tests for angle snapping, grid snapping, and dynamic snap radius calculation
- **SpatialGridTest**: Tests for spatial indexing, point insertion, and query performance
- **SnapPointCollectorTest**: Tests for intersection detection and snap point collection
- **UndoRedoManagerTest**: Tests for history management and state restoration
- **DrawingViewModelTest**: Integration tests for the main ViewModel

### Integration Tests
- **UserWorkflowTest**: End-to-end tests simulating real user interactions and workflows

### Performance Tests
- **PerformanceTest**: Performance regression tests and scalability validation

## Test Configuration

### TestConfiguration.kt
- Common test constants and thresholds
- Test data generators
- Performance measurement utilities

### TestUtils.kt
- Helper functions for creating test data
- Custom assertions for domain objects
- Test data factories

## Running Tests

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Classes
```bash
# Unit tests
./gradlew test --tests "com.rkt.snappyrulerset.domain.entity.GeometryTest"
./gradlew test --tests "com.rkt.snappyrulerset.domain.usecase.SnappingEngineTest"
./gradlew test --tests "com.rkt.snappyrulerset.domain.usecase.SpatialGridTest"

# Integration tests
./gradlew test --tests "com.rkt.snappyrulerset.integration.UserWorkflowTest"

# Performance tests
./gradlew test --tests "com.rkt.snappyrulerset.performance.PerformanceTest"
```

### Run Tests with Coverage
```bash
./gradlew testDebugUnitTestCoverage
```

## Test Coverage

The test suite covers:
- **Geometry Functions**: 100% coverage of Vec2 operations and geometric calculations
- **Snapping Engine**: 100% coverage of snapping algorithms and utilities
- **Spatial Grid**: 100% coverage of spatial indexing and query operations
- **State Management**: 100% coverage of undo/redo functionality
- **ViewModels**: 100% coverage of state management and user interactions
- **User Workflows**: End-to-end testing of common user scenarios

## Performance Benchmarks

The performance tests validate:
- **Spatial Grid**: < 1000ms for 10k point insertions, < 100ms for 1k queries
- **Line Processing**: < 500ms for 1k line point collections
- **Intersection Detection**: < 1000ms for 100 line intersections
- **Memory Usage**: < 10MB for 10k points
- **Query Performance**: < 1ms average query time

## Test Data

### Test Constants
- `TEST_DPI`: 160f (standard Android DPI)
- `TEST_GRID_SPACING_MM`: 5f (5mm grid spacing)
- `TEST_ZOOM_LEVEL`: 1f (normal zoom)
- `TEST_SNAP_RADIUS`: 16f (base snap radius)

### Performance Thresholds
- `MAX_INSERTION_TIME_MS`: 1000ms
- `MAX_QUERY_TIME_MS`: 100ms
- `MAX_INTERSECTION_TIME_MS`: 500ms
- `MAX_MEMORY_USAGE_MB`: 10MB

## Best Practices

### Writing Tests
1. Use descriptive test names that explain the scenario
2. Follow the Arrange-Act-Assert pattern
3. Use TestUtils for creating test data
4. Include both positive and negative test cases
5. Test edge cases and boundary conditions

### Performance Testing
1. Use TestConfiguration.measureTime() for timing measurements
2. Use TestConfiguration.measureMemory() for memory measurements
3. Include performance assertions with defined thresholds
4. Print performance results for monitoring

### Integration Testing
1. Test complete user workflows
2. Verify state changes at each step
3. Test undo/redo functionality
4. Validate snapping behavior with real data

## Continuous Integration

The test suite is designed to run in CI/CD pipelines:
- All tests are deterministic and don't depend on external resources
- Performance tests have defined thresholds for regression detection
- Tests run quickly (< 30 seconds total)
- Coverage reports are generated automatically

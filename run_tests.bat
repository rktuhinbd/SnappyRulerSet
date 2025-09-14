@echo off
REM Snappy Ruler Set - Test Runner Script for Windows
REM This script provides convenient commands for running different types of tests

echo 🎯 Snappy Ruler Set - Test Runner
echo =================================

if "%1"=="unit" goto unit_tests
if "%1"=="integration" goto integration_tests
if "%1"=="performance" goto performance_tests
if "%1"=="all" goto all_tests
if "%1"=="coverage" goto coverage_tests
if "%1"=="clean" goto clean_tests
if "%1"=="help" goto show_help
if "%1"=="-h" goto show_help
if "%1"=="--help" goto show_help
if "%1"=="" goto all_tests

echo ❌ Unknown command: %1
echo.
goto show_help

:unit_tests
echo 📋 Running Unit Tests...
echo ------------------------
echo 🔧 Geometry Tests...
gradlew test --tests "com.rkt.snappyrulerset.domain.entity.GeometryTest"
echo 🎯 Snapping Engine Tests...
gradlew test --tests "com.rkt.snappyrulerset.domain.usecase.SnappingEngineTest"
echo 📊 Spatial Grid Tests...
gradlew test --tests "com.rkt.snappyrulerset.domain.usecase.SpatialGridTest"
echo 🔄 Undo/Redo Manager Tests...
gradlew test --tests "com.rkt.snappyrulerset.domain.entity.UndoRedoManagerTest"
echo 🎨 ViewModel Tests...
gradlew test --tests "com.rkt.snappyrulerset.presentation.viewmodel.DrawingViewModelTest"
echo ✅ Unit tests completed successfully!
goto end

:integration_tests
echo 🔗 Running Integration Tests...
echo -------------------------------
gradlew test --tests "com.rkt.snappyrulerset.integration.UserWorkflowTest"
echo ✅ Integration tests completed successfully!
goto end

:performance_tests
echo ⚡ Running Performance Tests...
echo ------------------------------
gradlew test --tests "com.rkt.snappyrulerset.performance.PerformanceTest"
echo ✅ Performance tests completed successfully!
goto end

:all_tests
echo 🚀 Running All Tests...
echo =======================
gradlew test
echo ✅ All tests completed successfully!
goto end

:coverage_tests
echo 📊 Running Tests with Coverage...
echo --------------------------------
gradlew testDebugUnitTestCoverage
echo ✅ Coverage report generated!
echo 📁 Check app/build/reports/jacoco/testDebugUnitTestCoverage/html/index.html
goto end

:clean_tests
echo 🧹 Cleaning and Running Tests...
echo --------------------------------
gradlew clean test
echo ✅ Clean test run completed successfully!
goto end

:show_help
echo Usage: %0 [COMMAND]
echo.
echo Commands:
echo   unit        Run unit tests only
echo   integration Run integration tests only
echo   performance Run performance tests only
echo   all         Run all tests (default)
echo   coverage    Run tests with coverage report
echo   clean       Clean and run all tests
echo   help        Show this help message
echo.
echo Examples:
echo   %0 unit        # Run only unit tests
echo   %0 coverage    # Run tests with coverage
echo   %0 clean       # Clean and run all tests
goto end

:end
echo.
echo 🎉 Test execution completed!
pause

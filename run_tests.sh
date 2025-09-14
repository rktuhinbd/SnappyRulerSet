#!/bin/bash

# Snappy Ruler Set - Test Runner Script
# This script provides convenient commands for running different types of tests

set -e

echo "🎯 Snappy Ruler Set - Test Runner"
echo "================================="

# Function to run unit tests
run_unit_tests() {
    echo "📋 Running Unit Tests..."
    echo "------------------------"
    
    echo "🔧 Geometry Tests..."
    ./gradlew test --tests "com.rkt.snappyrulerset.domain.entity.GeometryTest"
    
    echo "🎯 Snapping Engine Tests..."
    ./gradlew test --tests "com.rkt.snappyrulerset.domain.usecase.SnappingEngineTest"
    
    echo "📊 Spatial Grid Tests..."
    ./gradlew test --tests "com.rkt.snappyrulerset.domain.usecase.SpatialGridTest"
    
    echo "🔄 Undo/Redo Manager Tests..."
    ./gradlew test --tests "com.rkt.snappyrulerset.domain.entity.UndoRedoManagerTest"
    
    echo "🎨 ViewModel Tests..."
    ./gradlew test --tests "com.rkt.snappyrulerset.presentation.viewmodel.DrawingViewModelTest"
    
    echo "✅ Unit tests completed successfully!"
}

# Function to run integration tests
run_integration_tests() {
    echo "🔗 Running Integration Tests..."
    echo "-------------------------------"
    
    ./gradlew test --tests "com.rkt.snappyrulerset.integration.UserWorkflowTest"
    
    echo "✅ Integration tests completed successfully!"
}

# Function to run performance tests
run_performance_tests() {
    echo "⚡ Running Performance Tests..."
    echo "------------------------------"
    
    ./gradlew test --tests "com.rkt.snappyrulerset.performance.PerformanceTest"
    
    echo "✅ Performance tests completed successfully!"
}

# Function to run all tests
run_all_tests() {
    echo "🚀 Running All Tests..."
    echo "======================="
    
    ./gradlew test
    
    echo "✅ All tests completed successfully!"
}

# Function to run tests with coverage
run_coverage() {
    echo "📊 Running Tests with Coverage..."
    echo "--------------------------------"
    
    ./gradlew testDebugUnitTestCoverage
    
    echo "✅ Coverage report generated!"
    echo "📁 Check app/build/reports/jacoco/testDebugUnitTestCoverage/html/index.html"
}

# Function to clean and run tests
clean_and_test() {
    echo "🧹 Cleaning and Running Tests..."
    echo "-------------------------------"
    
    ./gradlew clean test
    
    echo "✅ Clean test run completed successfully!"
}

# Function to show help
show_help() {
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  unit        Run unit tests only"
    echo "  integration Run integration tests only"
    echo "  performance Run performance tests only"
    echo "  all         Run all tests (default)"
    echo "  coverage    Run tests with coverage report"
    echo "  clean       Clean and run all tests"
    echo "  help        Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 unit        # Run only unit tests"
    echo "  $0 coverage    # Run tests with coverage"
    echo "  $0 clean       # Clean and run all tests"
}

# Main script logic
case "${1:-all}" in
    "unit")
        run_unit_tests
        ;;
    "integration")
        run_integration_tests
        ;;
    "performance")
        run_performance_tests
        ;;
    "all")
        run_all_tests
        ;;
    "coverage")
        run_coverage
        ;;
    "clean")
        clean_and_test
        ;;
    "help"|"-h"|"--help")
        show_help
        ;;
    *)
        echo "❌ Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac

echo ""
echo "🎉 Test execution completed!"

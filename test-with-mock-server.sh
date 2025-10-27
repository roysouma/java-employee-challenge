#!/bin/bash

# Function to cleanup on exit
cleanup() {
    if [ ! -z "$MOCK_SERVER_PID" ]; then
        echo ""
        echo "Stopping mock server (PID: $MOCK_SERVER_PID)..."
        kill $MOCK_SERVER_PID 2>/dev/null
        wait $MOCK_SERVER_PID 2>/dev/null
        echo "✅ Mock server stopped"
    fi
}

trap cleanup EXIT

echo "=== Employee API Test Suite (Full Automated) ==="
echo ""

echo "🧹 Cleaning previous builds..."
./gradlew clean --quiet
echo "✅ Clean completed"

echo ""
echo "1. Running Unit Tests with Coverage..."
./gradlew api:unitTestCoverage

if [ $? -eq 0 ]; then
    echo "✅ Unit tests passed!"
else
    echo "❌ Unit tests failed!"
    exit 1
fi

echo ""
echo "Ensuring mock server is stopped if it's running..."
pkill -f "server:bootRun" 2>/dev/null || true
lsof -ti:8112 | xargs kill -9 2>/dev/null || true
sleep 3
echo "✅ Mock server stopped (if it was running)"
echo "2. Starting mock server..."
./gradlew server:bootRun --quiet &
MOCK_SERVER_PID=$!
echo "Mock server starting (PID: $MOCK_SERVER_PID)..."

echo "⏳ Waiting for mock server to be ready..."
for i in {1..30}; do
    if curl -s http://localhost:8112/api/v1/employee > /dev/null 2>&1; then
        echo "✅ Mock server is ready!"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ Mock server failed to start within 30 seconds"
        exit 1
    fi
    sleep 1
done

echo ""
echo "3. Running Full Integration Tests with Mock Server..."
./gradlew -Dmock.server.running=true api:test --tests "com.reliaquest.api.integration.EmployeeWithMockServerIntegrationTest"

if [ $? -eq 0 ]; then
    echo "✅ Full integration tests passed!"
else
    echo "❌ Full integration tests failed!"
    exit 1
fi

echo ""
echo "ALL TESTS COMPLETED SUCCESSFULLY!"

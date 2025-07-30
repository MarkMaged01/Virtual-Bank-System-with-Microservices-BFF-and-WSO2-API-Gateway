# Test Logging Service Script
# This script helps you test the logging service step by step

Write-Host "=== Virtual Bank Logging Service Test ===" -ForegroundColor Green

# Step 1: Check if Java is installed
Write-Host "`n1. Checking Java installation..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "✅ Java found: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Java not found. Please install Java 11+" -ForegroundColor Red
    exit 1
}

# Step 2: Check if Maven is installed
Write-Host "`n2. Checking Maven installation..." -ForegroundColor Yellow
try {
    $mvnVersion = mvn -version 2>&1 | Select-String "Apache Maven"
    Write-Host "✅ Maven found: $mvnVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Maven not found. Please install Maven" -ForegroundColor Red
    exit 1
}

# Step 3: Check if Kafka is available
Write-Host "`n3. Checking Kafka installation..." -ForegroundColor Yellow
if (Test-Path "../kafka") {
    Write-Host "✅ Kafka found in ../kafka directory" -ForegroundColor Green
} else {
    Write-Host "⚠️  Kafka not found in ../kafka directory" -ForegroundColor Yellow
    Write-Host "   You can still test the logging service without Kafka" -ForegroundColor Yellow
}

# Step 4: Build the project
Write-Host "`n4. Building the project..." -ForegroundColor Yellow
try {
    mvn clean install -q
    Write-Host "✅ Project built successfully" -ForegroundColor Green
} catch {
    Write-Host "❌ Build failed" -ForegroundColor Red
    exit 1
}

# Step 5: Start the logging service with H2 database
Write-Host "`n5. Starting logging service with H2 database..." -ForegroundColor Yellow
Write-Host "   This will start the service without requiring MySQL" -ForegroundColor Cyan

# Start the service in background
$process = Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run", "-Dspring.profiles.active=h2" -PassThru -WindowStyle Hidden

# Wait for service to start
Write-Host "   Waiting for service to start..." -ForegroundColor Cyan
Start-Sleep -Seconds 30

# Step 6: Test the service
Write-Host "`n6. Testing the service..." -ForegroundColor Yellow

# Test health endpoint
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8090/api/logs/health" -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Health check passed: $($response.Content)" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Health check returned status: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Health check failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test logs endpoint
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8090/api/logs" -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Logs endpoint working" -ForegroundColor Green
        $logs = $response.Content | ConvertFrom-Json
        Write-Host "   Found $($logs.Count) log entries" -ForegroundColor Cyan
    } else {
        Write-Host "⚠️  Logs endpoint returned status: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Logs endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test stats endpoint
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8090/api/logs/stats" -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Stats endpoint working" -ForegroundColor Green
        $stats = $response.Content | ConvertFrom-Json
        Write-Host "   Total logs: $($stats.totalLogs)" -ForegroundColor Cyan
    } else {
        Write-Host "⚠️  Stats endpoint returned status: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Stats endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 7: Show useful information
Write-Host "`n7. Service Information:" -ForegroundColor Yellow
Write-Host "   Service URL: http://localhost:8090" -ForegroundColor Cyan
Write-Host "   Health Check: http://localhost:8090/api/logs/health" -ForegroundColor Cyan
Write-Host "   All Logs: http://localhost:8090/api/logs" -ForegroundColor Cyan
Write-Host "   Statistics: http://localhost:8090/api/logs/stats" -ForegroundColor Cyan
Write-Host "   H2 Console: http://localhost:8090/h2-console" -ForegroundColor Cyan

Write-Host "`n8. Next Steps:" -ForegroundColor Yellow
Write-Host "   - Start Kafka to test message consumption" -ForegroundColor Cyan
Write-Host "   - Start other services to generate logs" -ForegroundColor Cyan
Write-Host "   - Use the REST API to view and manage logs" -ForegroundColor Cyan

Write-Host "`n✅ Logging Service Test Complete!" -ForegroundColor Green
Write-Host "   The service is running in the background." -ForegroundColor Cyan
Write-Host "   Press Ctrl+C to stop the service when done." -ForegroundColor Yellow

# Keep the script running to maintain the service
try {
    Wait-Process -Id $process.Id
} catch {
    Write-Host "`nService stopped." -ForegroundColor Yellow
} 
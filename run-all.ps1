# Start Pact Broker
Write-Host "Starting Pact Broker..." -ForegroundColor Green
docker-compose up -d

Write-Host "`nWaiting for Pact Broker to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Check if broker is accessible
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9292" -UseBasicParsing -TimeoutSec 5
    Write-Host "✓ Pact Broker is running!" -ForegroundColor Green
    Write-Host "  Access at: http://localhost:9292" -ForegroundColor Cyan
    Write-Host "  Username: pact" -ForegroundColor Cyan
    Write-Host "  Password: pact" -ForegroundColor Cyan
} catch {
    Write-Host "⚠ Pact Broker might still be starting up. Please wait a moment." -ForegroundColor Yellow
}

# Run Consumer Tests
Write-Host "`n=== Running Consumer Tests ===" -ForegroundColor Green
Set-Location consumer
mvn clean test

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Consumer tests passed!" -ForegroundColor Green
    
    # Publish Pacts
    Write-Host "`n=== Publishing Pacts to Broker ===" -ForegroundColor Green
    mvn pact:publish
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Pacts published successfully!" -ForegroundColor Green
    } else {
        Write-Host "✗ Failed to publish pacts" -ForegroundColor Red
        Set-Location ..
        exit 1
    }
} else {
    Write-Host "✗ Consumer tests failed" -ForegroundColor Red
    Set-Location ..
    exit 1
}

Set-Location ..

# Run Provider Verification
Write-Host "`n=== Running Provider Verification ===" -ForegroundColor Green
Set-Location provider
mvn clean test

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Provider verification passed!" -ForegroundColor Green
} else {
    Write-Host "✗ Provider verification failed" -ForegroundColor Red
    Set-Location ..
    exit 1
}

Set-Location ..

Write-Host "`n=== All Done! ===" -ForegroundColor Green
Write-Host "Check the Pact Broker UI at http://localhost:9292" -ForegroundColor Cyan
Write-Host "Username: pact, Password: pact" -ForegroundColor Cyan
Write-Host "`nYou should see the UserConsumer → UserProvider relationship with verification status." -ForegroundColor Yellow

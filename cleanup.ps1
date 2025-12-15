Write-Host "Stopping Pact Broker and cleaning up..." -ForegroundColor Yellow
docker-compose down

Write-Host "Cleaning Maven projects..." -ForegroundColor Yellow
Set-Location consumer
mvn clean
Set-Location ..\provider
mvn clean
Set-Location ..

Write-Host "✓ Cleanup complete!" -ForegroundColor Green

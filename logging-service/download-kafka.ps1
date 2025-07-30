# Download and Setup Kafka Binary Distribution
# This script downloads Kafka binary distribution and extracts it to the correct location

Write-Host "Downloading Kafka Binary Distribution..." -ForegroundColor Green

# Create kafka directory if it doesn't exist
$kafkaDir = "..\kafka"
if (!(Test-Path $kafkaDir)) {
    New-Item -ItemType Directory -Path $kafkaDir -Force
}

# Download Kafka binary distribution
$kafkaVersion = "3.9.1"
$scalaVersion = "2.13"
$kafkaUrl = "https://downloads.apache.org/kafka/$kafkaVersion/kafka_$scalaVersion-$kafkaVersion.tgz"
$downloadPath = "kafka_$scalaVersion-$kafkaVersion.tgz"

Write-Host "Downloading from: $kafkaUrl" -ForegroundColor Yellow

try {
    # Download the file
    Invoke-WebRequest -Uri $kafkaUrl -OutFile $downloadPath
    
    Write-Host "Download completed. Extracting..." -ForegroundColor Green
    
    # Extract using tar (available in Windows 10+)
    tar -xzf $downloadPath -C $kafkaDir --strip-components=1
    
    # Clean up downloaded file
    Remove-Item $downloadPath
    
    Write-Host "Kafka setup completed!" -ForegroundColor Green
    Write-Host "Kafka is now available at: $kafkaDir" -ForegroundColor Yellow
    Write-Host "You can now run: .\start-kafka.bat" -ForegroundColor Yellow
    
} catch {
    Write-Host "Error downloading Kafka: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Please download manually from: https://kafka.apache.org/download" -ForegroundColor Yellow
    Write-Host "Extract to: $kafkaDir" -ForegroundColor Yellow
} 
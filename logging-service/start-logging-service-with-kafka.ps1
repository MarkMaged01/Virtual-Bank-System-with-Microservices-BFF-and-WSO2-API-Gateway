# Start Logging Service with Kafka
Write-Host "Starting Logging Service with Kafka..." -ForegroundColor Green

# Set Kafka environment variables
$env:KAFKA_HOME = "kafka"
$env:JAVA_HOME = $env:JAVA_HOME

# Check if Kafka directory exists
if (!(Test-Path "$env:KAFKA_HOME\bin\windows\kafka-server-start.bat")) {
    Write-Host "ERROR: Kafka binary distribution not found at $env:KAFKA_HOME" -ForegroundColor Red
    Write-Host "Please ensure Kafka is properly installed in the kafka directory" -ForegroundColor Yellow
    exit 1
}

# Create Kafka directories if they don't exist
if (!(Test-Path "kafka-logs")) { New-Item -ItemType Directory -Path "kafka-logs" -Force }
if (!(Test-Path "zookeeper-data")) { New-Item -ItemType Directory -Path "zookeeper-data" -Force }

Write-Host "Starting Zookeeper..." -ForegroundColor Yellow
Start-Process -FilePath "cmd" -ArgumentList "/k", "$env:KAFKA_HOME\bin\windows\zookeeper-server-start.bat $env:KAFKA_HOME\config\zookeeper.properties" -WindowStyle Minimized

# Wait for Zookeeper to start
Write-Host "Waiting for Zookeeper to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host "Starting Kafka Server..." -ForegroundColor Yellow
Start-Process -FilePath "cmd" -ArgumentList "/k", "$env:KAFKA_HOME\bin\windows\kafka-server-start.bat $env:KAFKA_HOME\config\server.properties" -WindowStyle Minimized

# Wait for Kafka to start
Write-Host "Waiting for Kafka to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

Write-Host "Creating Kafka topic with 3 partitions..." -ForegroundColor Yellow
# Use shorter command to avoid "input line is too long" error
$topicCmd = "$env:KAFKA_HOME\bin\windows\kafka-topics.bat"
$topicArgs = @("--create", "--topic", "virtualbank-logs", "--bootstrap-server", "localhost:9092", "--partitions", "3", "--replication-factor", "1", "--if-not-exists")
& $topicCmd @topicArgs

Write-Host "Kafka setup completed!" -ForegroundColor Green
Write-Host "Zookeeper is running on port 2181" -ForegroundColor Cyan
Write-Host "Kafka is running on port 9092" -ForegroundColor Cyan
Write-Host "Topic 'virtualbank-logs' created with 3 partitions" -ForegroundColor Cyan

Write-Host ""
Write-Host "Starting Logging Service..." -ForegroundColor Green
Write-Host ""

# Start the logging service
mvn spring-boot:run 
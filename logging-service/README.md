check the producers # Virtual Bank Logging Service

This service acts as a centralized logging system for the Virtual Bank microservices architecture. It consumes log messages from Kafka and stores them in a MySQL database for later analysis and debugging.

## Architecture Overview

```
Microservices → Kafka Topic → Logging Service → MySQL Database
```

1. **Microservices** send log messages to Kafka topic `virtualbank-logs`
2. **Logging Service** consumes messages from Kafka and stores them in the `dump` table
3. **MySQL Database** stores all log entries with proper indexing for fast queries

## Message Format

The logging service expects messages in this JSON format:

```json
{
  "message": "<escaped JSON request or response>",
  "messageType": "Request" | "Response" | "Error",
  "dateTime": "2024-01-01T12:00:00"
}
```

## Database Schema

The `dump` table is automatically created by Hibernate with this structure:

```sql
CREATE TABLE dump (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message TEXT NOT NULL,
    message_type ENUM('Request', 'Response', 'Error') NOT NULL,
    date_time TIMESTAMP NOT NULL
);
```

## Prerequisites

1. **Java 11+** installed
2. **MySQL** running on port 3307
3. **Kafka 3.9.1** downloaded and extracted to `../kafka` folder
4. **Maven** for building the project

## Setup Instructions

### 1. Create Database

Run this SQL script in MySQL:

```sql
CREATE DATABASE IF NOT EXISTS logging_service_db;
GRANT ALL PRIVILEGES ON logging_service_db.* TO 'vbankuser'@'%';
FLUSH PRIVILEGES;
```

Or use the provided script:
```bash
mysql -u root -p < create-database.sql
```

### 2. Start Kafka

**Windows:**
```bash
start-kafka.bat
```

**Linux/Mac:**
```bash
chmod +x start-kafka.sh
./start-kafka.sh
```

This will:
- Start Zookeeper on port 2181
- Start Kafka on port 9092
- Create topic `virtualbank-logs` with 3 partitions

### 3. Build and Run the Service

```bash
mvn clean install
mvn spring-boot:run
```

The service will start on port 8090.

## API Endpoints

### Health Check
```
GET /api/logs/health
```

### Get All Logs
```
GET /api/logs
```

### Get Logs by Type
```
GET /api/logs/type/{messageType}
```
Where `{messageType}` is: `Request`, `Response`, or `Error`

### Get Logs by Date Range
```
GET /api/logs/date-range?startDate=2024-01-01T00:00:00&endDate=2024-01-02T00:00:00
```

### Search Logs
```
GET /api/logs/search?searchText=userId
```

### Get Log Statistics
```
GET /api/logs/stats
```

### Get Log by ID
```
GET /api/logs/{id}
```

### Delete Log
```
DELETE /api/logs/{id}
```

## Kafka Topic Configuration

- **Topic Name**: `virtualbank-logs`
- **Partitions**: 3 (for better parallelism)
- **Replication Factor**: 1 (for development)
- **Consumer Group**: `logging-service-group`

## Configuration Properties

Key configuration in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3307/logging_service_db
spring.jpa.hibernate.ddl-auto=create-drop

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
kafka.topic.logging=virtualbank-logs
kafka.consumer.group-id=logging-service-group
```

## Testing the Service

### 1. Send Test Message to Kafka

Using Kafka console producer:
```bash
../kafka/bin/kafka-console-producer.sh --topic virtualbank-logs --bootstrap-server localhost:9092
```

Then send this message:
```json
{"message": "{\"test\": \"Sample request\"}", "messageType": "Request", "dateTime": "2024-01-01T12:00:00"}
```

### 2. Check if Message was Stored

```bash
curl http://localhost:8090/api/logs
```

### 3. View Statistics

```bash
curl http://localhost:8090/api/logs/stats
```

## Integration with Other Services

To integrate this logging service with other microservices, they need to:

1. **Add Kafka producer dependency** to their `pom.xml`
2. **Configure Kafka producer** in their application
3. **Send log messages** to the `virtualbank-logs` topic

Example message format for other services:
```json
{
  "message": "{\"userId\": \"123\", \"action\": \"login\", \"status\": \"success\"}",
  "messageType": "Request",
  "dateTime": "2024-01-01T12:00:00"
}
```

## Troubleshooting

### Common Issues

1. **Kafka connection failed**: Make sure Kafka is running on port 9092
2. **Database connection failed**: Check MySQL is running on port 3307
3. **Topic not found**: Run the Kafka startup script to create the topic
4. **Permission denied**: Make sure the database user has proper privileges

### Logs Location

- **Application logs**: Console output
- **Kafka logs**: `kafka-logs/` directory
- **Zookeeper logs**: `zookeeper-data/` directory

## Performance Considerations

- **Indexes**: The database has indexes on `message_type`, `date_time`, and composite indexes
- **Batch processing**: Kafka consumer processes messages in batches
- **Concurrency**: Uses 3 consumer threads for parallel processing
- **Memory**: Configured for optimal memory usage

## Security Notes

- Database credentials are in plain text (use environment variables in production)
- CORS is enabled for all origins (restrict in production)
- No authentication on REST endpoints (add security in production) 
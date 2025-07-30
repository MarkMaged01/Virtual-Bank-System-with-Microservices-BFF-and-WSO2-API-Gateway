#!/bin/bash

echo "Starting Kafka for Virtual Bank System..."

# Set Kafka environment variables
export KAFKA_HOME=../kafka
export JAVA_HOME=${JAVA_HOME}

# Create Kafka directories if they don't exist
mkdir -p kafka-logs
mkdir -p zookeeper-data

echo "Starting Zookeeper..."
$KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties &
ZOOKEEPER_PID=$!

# Wait for Zookeeper to start
sleep 10

echo "Starting Kafka Server..."
$KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties &
KAFKA_PID=$!

# Wait for Kafka to start
sleep 15

echo "Creating Kafka topic with 3 partitions..."
$KAFKA_HOME/bin/kafka-topics.sh --create --topic virtualbank-logs --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1 --if-not-exists

echo "Kafka setup completed!"
echo "Zookeeper is running on port 2181 (PID: $ZOOKEEPER_PID)"
echo "Kafka is running on port 9092 (PID: $KAFKA_PID)"
echo "Topic 'virtualbank-logs' created with 3 partitions"

# Function to cleanup on exit
cleanup() {
    echo "Shutting down Kafka..."
    kill $KAFKA_PID
    echo "Shutting down Zookeeper..."
    kill $ZOOKEEPER_PID
    exit 0
}

# Set trap to cleanup on script exit
trap cleanup SIGINT SIGTERM

# Keep script running
wait 
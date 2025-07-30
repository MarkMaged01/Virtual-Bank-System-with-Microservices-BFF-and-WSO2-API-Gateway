package com.virtualbank.loggingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Main Spring Boot application class for the Logging Service
 * This service acts as a Kafka consumer to collect and store log messages
 * from all microservices in the Virtual Bank System
 */
@SpringBootApplication
@EnableKafka
public class LoggingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoggingServiceApplication.class, args);
        System.out.println("=== Virtual Bank Logging Service Started ===");
        System.out.println("Service is listening for log messages on Kafka topic: virtualbank-logs");
        System.out.println("Database table 'dump' will be created automatically");
        System.out.println("=============================================");
    }
} 
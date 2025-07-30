package com.virtualbank.accountservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class LoggingService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${kafka.topic.logging:virtualbank-logs}")
    private String loggingTopic;

    /**
     * Log a request message to Kafka
     * @param serviceName The name of the service (for internal use)
     * @param endpoint The endpoint being called (for internal use)
     * @param request The request object to log
     */
    public void logRequest(String serviceName, String endpoint, Object request) {
        try {
            // Create enhanced message with service context
            Map<String, Object> enhancedRequest = new HashMap<>();
            enhancedRequest.put("serviceName", serviceName);
            enhancedRequest.put("endpoint", endpoint);
            enhancedRequest.put("data", request);
            
            Map<String, Object> logMessage = new HashMap<>();
            logMessage.put("message", objectMapper.writeValueAsString(enhancedRequest));
            logMessage.put("messageType", "Request");
            logMessage.put("dateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            String logJson = objectMapper.writeValueAsString(logMessage);
            kafkaTemplate.send(loggingTopic, logJson);
            logger.debug("Request logged to Kafka: {}", logJson);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing request log: {}", e.getMessage());
        }
    }

    /**
     * Log a response message to Kafka
     * @param serviceName The name of the service (for internal use)
     * @param endpoint The endpoint being called (for internal use)
     * @param response The response object to log
     */
    public void logResponse(String serviceName, String endpoint, Object response) {
        try {
            // Create enhanced message with service context
            Map<String, Object> enhancedResponse = new HashMap<>();
            enhancedResponse.put("serviceName", serviceName);
            enhancedResponse.put("endpoint", endpoint);
            enhancedResponse.put("data", response);
            
            Map<String, Object> logMessage = new HashMap<>();
            logMessage.put("message", objectMapper.writeValueAsString(enhancedResponse));
            logMessage.put("messageType", "Response");
            logMessage.put("dateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            String logJson = objectMapper.writeValueAsString(logMessage);
            kafkaTemplate.send(loggingTopic, logJson);
            logger.debug("Response logged to Kafka: {}", logJson);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing response log: {}", e.getMessage());
        }
    }

    /**
     * Log an error message to Kafka
     * @param serviceName The name of the service (for internal use)
     * @param endpoint The endpoint being called (for internal use)
     * @param errorMessage The error message to log
     */
    public void logError(String serviceName, String endpoint, String errorMessage) {
        try {
            // Create enhanced error message with service context
            Map<String, Object> enhancedError = new HashMap<>();
            enhancedError.put("serviceName", serviceName);
            enhancedError.put("endpoint", endpoint);
            enhancedError.put("error", errorMessage);
            
            Map<String, Object> logMessage = new HashMap<>();
            logMessage.put("message", objectMapper.writeValueAsString(enhancedError));
            logMessage.put("messageType", "Error");
            logMessage.put("dateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            String logJson = objectMapper.writeValueAsString(logMessage);
            kafkaTemplate.send(loggingTopic, logJson);
            logger.debug("Error logged to Kafka: {}", logJson);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing error log: {}", e.getMessage());
        }
    }
} 